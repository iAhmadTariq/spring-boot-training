const API_BASE = '/api/v1/news';
const SORT_BY = 'reportedAt';
const SORT_DIRECTION = 'desc';

let pageItems = []; // articles on the currently loaded page
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let totalElements = 0;

let dialogMode = 'create'; // 'create' | 'edit'
let pendingDeleteId = null;

const grid = document.getElementById('news-grid');
const loadingEl = document.getElementById('loading');
const emptyEl = document.getElementById('empty-state');
const errorBanner = document.getElementById('error-banner');
const resultCount = document.getElementById('result-count');
const searchInput = document.getElementById('search');

const paginationEl = document.getElementById('pagination');
const pageIndicator = document.getElementById('page-indicator');
const btnPrevPage = document.getElementById('btn-prev-page');
const btnNextPage = document.getElementById('btn-next-page');
const pageSizeSelect = document.getElementById('page-size-select');

const articleDialog = document.getElementById('article-dialog');
const articleForm = document.getElementById('article-form');
const dialogTitle = document.getElementById('dialog-title');
const formError = document.getElementById('form-error');
const fieldId = document.getElementById('field-id');
const fieldTitle = document.getElementById('field-title');
const fieldAuthor = document.getElementById('field-author');
const fieldDate = document.getElementById('field-date');
const fieldDescription = document.getElementById('field-description');

const confirmDialog = document.getElementById('confirm-dialog');
const confirmMessage = document.getElementById('confirm-message');

const toastContainer = document.getElementById('toast-container');

function escapeHtml(value) {
  const div = document.createElement('div');
  div.textContent = value ?? '';
  return div.innerHTML;
}

function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  toastContainer.appendChild(toast);
  setTimeout(() => toast.remove(), 3200);
}

function showErrorBanner(message) {
  errorBanner.textContent = message;
  errorBanner.classList.remove('hidden');
}

function clearErrorBanner() {
  errorBanner.classList.add('hidden');
  errorBanner.textContent = '';
}

function formatDisplayDate(isoString) {
  if (!isoString) return '—';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return isoString;
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function toDatetimeLocalValue(isoString) {
  if (!isoString) return '';
  return isoString.slice(0, 16);
}

async function apiRequest(path, options = {}) {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!response.ok) {
    let detail = '';
    try {
      const body = await response.json();
      detail = body.message || body.error || '';
    } catch {
      // response had no JSON body
    }
    throw new Error(detail || `Request failed (${response.status})`);
  }
  if (response.status === 204) return null;
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

// Normalizes a Spring Data Page<News> response (content + page metadata) into
// a plain { items, number, size, totalElements, totalPages } shape. Falls back
// to handling a raw array, in case the endpoint ever reverts to returning List<News>.
function parsePageResponse(body) {
  if (Array.isArray(body)) {
    return {
      items: body,
      number: 0,
      size: body.length,
      totalElements: body.length,
      totalPages: body.length ? 1 : 0,
    };
  }
  if (body && Array.isArray(body.content)) {
    const meta = body.page || body; // newer Spring Data nests metadata under "page"
    return {
      items: body.content,
      number: meta.number ?? 0,
      size: meta.size ?? pageSize,
      totalElements: meta.totalElements ?? body.content.length,
      totalPages: meta.totalPages ?? 1,
    };
  }
  return { items: [], number: 0, size: pageSize, totalElements: 0, totalPages: 0 };
}

function buildNewsUrl(page) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(pageSize),
    sortBy: SORT_BY,
    direction: SORT_DIRECTION,
  });
  return `${API_BASE}?${params.toString()}`;
}

async function loadNews(page = currentPage) {
  loadingEl.classList.remove('hidden');
  emptyEl.classList.add('hidden');
  grid.classList.add('hidden');
  paginationEl.classList.add('hidden');
  clearErrorBanner();
  try {
    const body = await apiRequest(buildNewsUrl(page));
    const parsed = parsePageResponse(body);

    // If we deleted the last item on a page beyond the first, step back one page.
    if (parsed.items.length === 0 && parsed.number > 0) {
      loadingEl.classList.add('hidden');
      await loadNews(parsed.number - 1);
      return;
    }

    pageItems = parsed.items;
    currentPage = parsed.number;
    totalPages = parsed.totalPages;
    totalElements = parsed.totalElements;

    renderGrid();
    renderPagination();
  } catch (err) {
    showErrorBanner(`Could not load articles: ${err.message}`);
  } finally {
    loadingEl.classList.add('hidden');
  }
}

function renderPagination() {
  if (totalElements === 0) {
    paginationEl.classList.add('hidden');
    return;
  }
  paginationEl.classList.remove('hidden');
  pageIndicator.textContent = `Page ${currentPage + 1} of ${Math.max(totalPages, 1)}`;
  btnPrevPage.disabled = currentPage <= 0;
  btnNextPage.disabled = currentPage >= totalPages - 1;
}

function renderGrid() {
  const query = searchInput.value.trim().toLowerCase();
  const filtered = query
    ? pageItems.filter(
        (n) =>
          n.title?.toLowerCase().includes(query) ||
          n.author?.toLowerCase().includes(query)
      )
    : pageItems;

  resultCount.textContent = totalElements
    ? query
      ? `${filtered.length} of ${pageItems.length} on this page (${totalElements} total)`
      : `${totalElements} article${totalElements === 1 ? '' : 's'} total`
    : '';

  if (filtered.length === 0) {
    grid.classList.add('hidden');
    grid.innerHTML = '';
    emptyEl.classList.remove('hidden');
    emptyEl.querySelector('p:not(.state-emoji)').textContent = query
      ? 'No articles on this page match your search.'
      : 'No articles yet.';
    document.getElementById('btn-empty-new').classList.toggle('hidden', Boolean(query));
    return;
  }

  emptyEl.classList.add('hidden');
  grid.classList.remove('hidden');
  grid.innerHTML = filtered.map(cardTemplate).join('');

  grid.querySelectorAll('[data-action="edit"]').forEach((btn) => {
    btn.addEventListener('click', () => openEditDialog(Number(btn.dataset.id)));
  });
  grid.querySelectorAll('[data-action="delete"]').forEach((btn) => {
    btn.addEventListener('click', () => openConfirmDialog(Number(btn.dataset.id)));
  });
}

function cardTemplate(news) {
  return `
    <article class="news-card">
      <div class="news-card-header">
        <h3>${escapeHtml(news.title)}</h3>
        <span class="news-id-badge">#${news.newsId}</span>
      </div>
      <div class="news-meta">
        <span>✍️ ${escapeHtml(news.author) || 'Unknown'}</span>
        <span>🕒 ${formatDisplayDate(news.reportedAt)}</span>
      </div>
      <p class="news-description">${escapeHtml(news.description) || 'No description provided.'}</p>
      <div class="news-card-actions">
        <button class="btn btn-icon" type="button" data-action="edit" data-id="${news.newsId}">✎ Edit</button>
        <button class="btn btn-icon danger" type="button" data-action="delete" data-id="${news.newsId}">🗑 Delete</button>
      </div>
    </article>
  `;
}

function nextSuggestedId() {
  if (pageItems.length === 0) return 1;
  return Math.max(...pageItems.map((n) => n.newsId)) + 1;
}

function openCreateDialog() {
  dialogMode = 'create';
  dialogTitle.textContent = 'New Article';
  articleForm.reset();
  formError.classList.add('hidden');
  fieldId.value = nextSuggestedId();
  fieldId.disabled = false;
  fieldDate.value = new Date().toISOString().slice(0, 16);
  articleDialog.showModal();
  fieldTitle.focus();
}

function openEditDialog(newsId) {
  const news = pageItems.find((n) => n.newsId === newsId);
  if (!news) return;
  dialogMode = 'edit';
  dialogTitle.textContent = `Edit Article #${news.newsId}`;
  formError.classList.add('hidden');
  fieldId.value = news.newsId;
  fieldId.disabled = true;
  fieldTitle.value = news.title || '';
  fieldAuthor.value = news.author || '';
  fieldDate.value = toDatetimeLocalValue(news.reportedAt);
  fieldDescription.value = news.description || '';
  articleDialog.showModal();
  fieldTitle.focus();
}

function closeArticleDialog() {
  articleDialog.close();
}

async function handleArticleSubmit(event) {
  event.preventDefault();
  formError.classList.add('hidden');

  const payload = {
    newsId: Number(fieldId.value),
    title: fieldTitle.value.trim(),
    author: fieldAuthor.value.trim(),
    reportedAt: fieldDate.value,
    description: fieldDescription.value.trim(),
  };

  if (!payload.title || !payload.author || !payload.reportedAt || !payload.newsId) {
    formError.textContent = 'Please fill in ID, title, author, and reported date.';
    formError.classList.remove('hidden');
    return;
  }

  const saveBtn = document.getElementById('btn-save');
  saveBtn.disabled = true;
  try {
    if (dialogMode === 'create') {
      if (pageItems.some((n) => n.newsId === payload.newsId)) {
        throw new Error(`News ID #${payload.newsId} is already in use on this page.`);
      }
      await apiRequest(API_BASE, { method: 'POST', body: JSON.stringify(payload) });
      showToast(`Article #${payload.newsId} created.`, 'success');
      closeArticleDialog();
      await loadNews(0); // jump to first page so the new/sorted article is visible
    } else {
      await apiRequest(`${API_BASE}/${payload.newsId}`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast(`Article #${payload.newsId} updated.`, 'success');
      closeArticleDialog();
      await loadNews();
    }
  } catch (err) {
    formError.textContent = err.message;
    formError.classList.remove('hidden');
  } finally {
    saveBtn.disabled = false;
  }
}

function openConfirmDialog(newsId) {
  const news = pageItems.find((n) => n.newsId === newsId);
  if (!news) return;
  pendingDeleteId = newsId;
  confirmMessage.textContent = `"${news.title}" (#${news.newsId}) will be permanently deleted.`;
  confirmDialog.showModal();
}

async function handleConfirmDelete() {
  if (pendingDeleteId == null) return;
  const id = pendingDeleteId;
  const deleteBtn = document.getElementById('btn-confirm-delete');
  deleteBtn.disabled = true;
  try {
    await apiRequest(`${API_BASE}/${id}`, { method: 'DELETE' });
    confirmDialog.close();
    showToast(`Article #${id} deleted.`, 'success');
    await loadNews();
  } catch (err) {
    showToast(`Could not delete: ${err.message}`, 'error');
  } finally {
    deleteBtn.disabled = false;
    pendingDeleteId = null;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadNews(0);

  document.getElementById('btn-new').addEventListener('click', openCreateDialog);
  document.getElementById('btn-empty-new').addEventListener('click', openCreateDialog);
  document.getElementById('btn-cancel').addEventListener('click', closeArticleDialog);
  articleForm.addEventListener('submit', handleArticleSubmit);

  document.getElementById('btn-confirm-cancel').addEventListener('click', () => confirmDialog.close());
  document.getElementById('btn-confirm-delete').addEventListener('click', handleConfirmDelete);

  searchInput.addEventListener('input', renderGrid);

  btnPrevPage.addEventListener('click', () => {
    if (currentPage > 0) loadNews(currentPage - 1);
  });
  btnNextPage.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadNews(currentPage + 1);
  });
  pageSizeSelect.addEventListener('change', () => {
    pageSize = Number(pageSizeSelect.value);
    loadNews(0);
  });
});
