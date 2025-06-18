document.addEventListener('DOMContentLoaded', function() {
    const extractButton = document.getElementById('extractButton');
    const resultDiv = document.getElementById('result');
    const loadingDiv = document.getElementById('loading');

    function showLoading() {
        loadingDiv.style.display = 'block';
        resultDiv.innerHTML = '';
    }

    function hideLoading() {
        loadingDiv.style.display = 'none';
    }

    function formatJobInfo(jobData) {
        let html = '<div class="success-message">Job information extracted successfully!</div>';
        
        // Add raw data display
        html += `
            <div class="job-info">
                <h4>Raw Response Data:</h4>
                <pre style="background: #f5f5f5; padding: 10px; overflow-x: auto;">${JSON.stringify(jobData, null, 2)}</pre>
            </div>
        `;

        // Only try to format if it's an object
        if (typeof jobData === 'object' && jobData !== null) {
            // Create sections for each field in the JobApplication
            const sections = {
                'Basic Information': [
                    { key: 'title', label: 'Job Title' },
                    { key: 'company', label: 'Company' },
                    { key: 'location', label: 'Location' }
                ],
                'Details': [
                    { key: 'description', label: 'Description' },
                    { key: 'requirements', label: 'Requirements' },
                    { key: 'salary', label: 'Salary' },
                    { key: 'employmentType', label: 'Employment Type' }
                ],
                'Additional Information': [
                    { key: 'postedDate', label: 'Posted Date' },
                    { key: 'applicationDeadline', label: 'Application Deadline' },
                    { key: 'contactInfo', label: 'Contact Information' }
                ]
            };

            // Generate HTML for each section
            for (const [sectionTitle, fields] of Object.entries(sections)) {
                html += `<div class="job-info">
                    <h4>${sectionTitle}</h4>`;
                
                fields.forEach(field => {
                    if (jobData[field.key]) {
                        html += `<p><strong>${field.label}:</strong> ${jobData[field.key]}</p>`;
                    }
                });
                
                html += '</div>';
            }
        } else {
            html += `<div class="job-info">
                <p><strong>Response:</strong> ${jobData}</p>
            </div>`;
        }

        return html;
    }

    function displayResult(data) {
        if (data) {
            resultDiv.innerHTML = formatJobInfo(data);
        }
    }

    function displayError(message) {
        resultDiv.innerHTML = `<div class="error">Error: ${message}</div>`;
    }

    extractButton.addEventListener('click', function() {
        showLoading();
        
        // Get the active tab
        chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
            if (!tabs[0]) {
                hideLoading();
                displayError('No active tab found');
                return;
            }

            // Send message directly to content script
            chrome.tabs.sendMessage(tabs[0].id, {action: 'extractJobInfo'}, function(response) {
                hideLoading();
                
                if (chrome.runtime.lastError) {
                    displayError('Failed to communicate with the page. Please refresh and try again.');
                    return;
                }

                if (response.status === 'success') {
                    displayResult(response.data);
                } else {
                    displayError(response.message || 'Failed to extract job information');
                }
            });
        });
    });
}); 