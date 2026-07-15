document.addEventListener('DOMContentLoaded', () => {
    const fetchBtn = document.getElementById('fetch-btn');
    const infoContainer = document.getElementById('info-container');
    const statusSpan = document.getElementById('status');
    const frameworkSpan = document.getElementById('framework');
    const runtimeSpan = document.getElementById('runtime');

    fetchBtn.addEventListener('click', () => {
        // Fetch dynamically from your controller endpoint
        fetch('/info')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Populate the data into HTML elements
                statusSpan.textContent = data.status;
                frameworkSpan.textContent = data.framework;
                runtimeSpan.textContent = data.runtime;

                // Show the container
                infoContainer.classList.remove('hidden');
            })
            .catch(error => {
                console.error('Error fetching data:', error);
                alert('Could not connect to the backend API mapping.');
            });
    });
});