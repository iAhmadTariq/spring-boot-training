// One card per News API endpoint. Each card lets you tweak params/body and
// execute a real request against the running API, then shows the raw JSON response.
const ENDPOINTS = [
  {
    id: 'list',
    method: 'GET',
    pathTemplate: '/api/v1/news',
    title: 'List Articles',
    description: 'Retrieve a paginated, sorted list of all news articles.',
    params: [
      { name: 'page', kind: 'query', type: 'number', value: 0 },
      { name: 'size', kind: 'query', type: 'number', value: 10 },
      { name: 'sortBy', kind: 'query', type: 'text', value: 'reportedAt' },
      { name: 'direction', kind: 'query', type: 'text', value: 'desc' },
    ],
  },
  {
    id: 'get',
    method: 'GET',
    pathTemplate: '/api/v1/news/{newsId}',
    title: 'Get Article',
    description: 'Retrieve a single news article by its ID.',
    params: [{ name: 'newsId', kind: 'path', type: 'number', value: 1 }],
  },
  {
    id: 'create',
    method: 'POST',
    pathTemplate: '/api/v1/news',
    title: 'Create Article',
    description: 'Publish a brand-new news article.',
    params: [],
    body: {
      newsId: 101,
      title: 'Sample News Title',
      description: 'A short, realistic sample description used to try out the create endpoint.',
      author: 'Jane Doe',
      reportedAt: '2026-07-15T09:00:00',
    },
  },
  {
    id: 'update',
    method: 'PUT',
    pathTemplate: '/api/v1/news/{newsId}',
    title: 'Replace Article',
    description: 'Fully replace an existing article. All fields are required.',
    params: [{ name: 'newsId', kind: 'path', type: 'number', value: 1 }],
    body: {
      newsId: 1,
      title: 'Spring Boot 4.0 Released (Updated)',
      description: 'Updated description highlighting first-class virtual threads support.',
      author: 'Jane Doe',
      reportedAt: '2026-07-15T09:00:00',
    },
  },
  {
    id: 'patch',
    method: 'PATCH',
    pathTemplate: '/api/v1/news/{newsId}',
    title: 'Update Article Fields',
    description: 'Partially update one or more fields on an existing article.',
    params: [{ name: 'newsId', kind: 'path', type: 'number', value: 1 }],
    body: {
      title: 'Spring Boot 4.0 Released — Now With Native Compilation',
    },
  },
  {
    id: 'delete',
    method: 'DELETE',
    pathTemplate: '/api/v1/news/{newsId}',
    title: 'Delete Article',
    description: 'Permanently remove an article by ID.',
    params: [{ name: 'newsId', kind: 'path', type: 'number', value: 1 }],
  },
];

const STATUS_TEXT = {
  200: 'OK',
  201: 'Created',
  204: 'No Content',
  400: 'Bad Request',
  401: 'Unauthorized',
  403: 'Forbidden',
  404: 'Not Found',
  405: 'Method Not Allowed',
  409: 'Conflict',
  500: 'Internal Server Error',
};

const grid = document.getElementById('endpoints-grid');

function escapeHtml(value) {
  const div = document.createElement('div');
  div.textContent = value ?? '';
  return div.innerHTML;
}

function paramFieldHtml(param, kindLabel) {
  return `
    <div class="param-field">
      <label>${escapeHtml(param.name)} <span class="field-tag">${kindLabel}</span></label>
      <input type="${param.type}" data-param="${escapeHtml(param.name)}" value="${escapeHtml(String(param.value))}" />
    </div>
  `;
}

function renderEndpointCard(endpoint) {
  const methodClass = `method-${endpoint.method.toLowerCase()}`;
  const pathParams = endpoint.params.filter((p) => p.kind === 'path');
  const queryParams = endpoint.params.filter((p) => p.kind === 'query');

  const paramsHtml = pathParams.length || queryParams.length
    ? `<div class="endpoint-params">
        ${pathParams.map((p) => paramFieldHtml(p, 'Path')).join('')}
        ${queryParams.map((p) => paramFieldHtml(p, 'Query')).join('')}
      </div>`
    : '';

  const bodyHtml = endpoint.body !== undefined
    ? `<div class="endpoint-body-field">
        <label>Request Body <span class="field-tag">JSON</span></label>
        <textarea class="json-editor" data-role="body" rows="7" spellcheck="false">${escapeHtml(JSON.stringify(endpoint.body, null, 2))}</textarea>
      </div>`
    : '';

  return `
    <article class="endpoint-card" data-endpoint-id="${endpoint.id}">
      <div class="endpoint-header">
        <span class="method-badge ${methodClass}">${endpoint.method}</span>
        <code class="endpoint-path">${endpoint.pathTemplate}</code>
      </div>
      <h3 class="endpoint-title">${endpoint.title}</h3>
      <p class="endpoint-desc">${endpoint.description}</p>
      ${paramsHtml}
      ${bodyHtml}
      <div class="endpoint-actions">
        <button type="button" class="btn-execute ${methodClass}" data-role="execute">
          <span class="run-icon" aria-hidden="true">▶</span> Execute
        </button>
        <span class="endpoint-meta" data-role="meta"></span>
      </div>
      <div class="endpoint-response hidden" data-role="response-wrap">
        <div class="response-header">
          <span class="response-status" data-role="status"></span>
          <button type="button" class="btn-copy" data-role="copy">⧉ Copy</button>
        </div>
        <pre class="response-body"><code data-role="response-body"></code></pre>
      </div>
    </article>
  `;
}

function setLoading(card, isLoading) {
  const btn = card.querySelector('[data-role="execute"]');
  btn.disabled = isLoading;
  btn.innerHTML = isLoading
    ? '<span class="spinner-sm" aria-hidden="true"></span> Running…'
    : '<span class="run-icon" aria-hidden="true">▶</span> Execute';
}

function showResponse(card, result) {
  const wrap = card.querySelector('[data-role="response-wrap"]');
  const statusEl = card.querySelector('[data-role="status"]');
  const bodyEl = card.querySelector('[data-role="response-body"]');
  const metaEl = card.querySelector('[data-role="meta"]');
  wrap.classList.remove('hidden');
  metaEl.textContent = result.elapsed != null ? `${result.elapsed} ms` : '';

  if (result.error) {
    statusEl.textContent = result.statusText;
    statusEl.className = 'response-status status-error';
    bodyEl.textContent = result.detail || 'Request failed.';
    return;
  }

  const statusClass =
    result.status >= 200 && result.status < 300 ? 'status-2xx' :
    result.status >= 400 && result.status < 500 ? 'status-4xx' :
    result.status >= 500 ? 'status-5xx' : 'status-other';
  statusEl.textContent = `${result.status} ${STATUS_TEXT[result.status] || ''}`.trim();
  statusEl.className = `response-status ${statusClass}`;
  bodyEl.textContent =
    result.data === null || result.data === undefined
      ? '(empty response body)'
      : typeof result.data === 'string'
        ? result.data
        : JSON.stringify(result.data, null, 2);
}

function buildUrl(endpoint, card) {
  let path = endpoint.pathTemplate;
  endpoint.params
    .filter((p) => p.kind === 'path')
    .forEach((p) => {
      const input = card.querySelector(`[data-param="${p.name}"]`);
      path = path.replace(`{${p.name}}`, encodeURIComponent(input.value || p.value));
    });

  const queryParams = endpoint.params.filter((p) => p.kind === 'query');
  if (queryParams.length === 0) return path;

  const qs = new URLSearchParams();
  queryParams.forEach((p) => {
    const input = card.querySelector(`[data-param="${p.name}"]`);
    qs.set(p.name, input.value || p.value);
  });
  return `${path}?${qs.toString()}`;
}

async function executeEndpoint(endpoint, card) {
  let requestBody;
  if (endpoint.body !== undefined) {
    const textarea = card.querySelector('[data-role="body"]');
    try {
      requestBody = textarea.value.trim() ? JSON.parse(textarea.value) : undefined;
    } catch (err) {
      showResponse(card, { error: true, statusText: 'Invalid JSON body', detail: err.message });
      return;
    }
  }

  const url = buildUrl(endpoint, card);
  setLoading(card, true);
  const started = performance.now();
  try {
    const response = await fetch(url, {
      method: endpoint.method,
      headers: requestBody !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: requestBody !== undefined ? JSON.stringify(requestBody) : undefined,
    });
    const elapsed = Math.round(performance.now() - started);
    const text = await response.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = text;
      }
    }
    showResponse(card, { status: response.status, elapsed, data });
  } catch (err) {
    showResponse(card, { error: true, statusText: 'Network error', detail: err.message, elapsed: Math.round(performance.now() - started) });
  } finally {
    setLoading(card, false);
  }
}

grid.addEventListener('click', (event) => {
  const executeBtn = event.target.closest('[data-role="execute"]');
  if (executeBtn) {
    const card = executeBtn.closest('.endpoint-card');
    const endpoint = ENDPOINTS.find((ep) => ep.id === card.dataset.endpointId);
    executeEndpoint(endpoint, card);
    return;
  }

  const copyBtn = event.target.closest('[data-role="copy"]');
  if (copyBtn) {
    const card = copyBtn.closest('.endpoint-card');
    const codeEl = card.querySelector('[data-role="response-body"]');
    navigator.clipboard?.writeText(codeEl.textContent).then(() => {
      const original = copyBtn.textContent;
      copyBtn.textContent = '✓ Copied';
      setTimeout(() => (copyBtn.textContent = original), 1200);
    });
  }
});

document.addEventListener('DOMContentLoaded', () => {
  grid.innerHTML = ENDPOINTS.map(renderEndpointCard).join('');
});
