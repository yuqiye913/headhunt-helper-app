// This script runs in the context of web pages
console.log('Content script loaded!');

// Function to get the full HTML content of the page
function getPageHtml() {
    const html = document.documentElement.outerHTML;
    console.log('HTML Content Length:', html.length);
    console.log('HTML Content Preview:', html.substring(0, 500) + '...');
    return html;
}

// Function to send HTML content to the server
async function sendHtmlToServer(htmlContent) {
    try {
        // Get the extension's ID
        const extensionId = chrome.runtime.id;
        const extensionOrigin = `chrome-extension://${extensionId}`;
        
        console.log('=== REQUEST DETAILS ===');
        console.log('URL:', 'http://localhost:8080/api/applications/html');
        console.log('Method:', 'POST');
        console.log('Extension Origin:', extensionOrigin);
        console.log('Headers:', {
            'Content-Type': 'text/plain',
            'Accept': 'application/json',
            'Origin': extensionOrigin
        });
        console.log('Body Length:', htmlContent.length);
        console.log('Body Preview:', htmlContent.substring(0, 500) + '...');
        console.log('=====================');

        // Main request
        const response = await fetch('http://localhost:8080/api/applications/html', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain',
                'Accept': 'application/json',
                'Origin': extensionOrigin
            },
            body: htmlContent
        });
        
        console.log('=== RESPONSE DETAILS ===');
        console.log('Status:', response.status);
        console.log('Status Text:', response.statusText);
        console.log('Headers:', Object.fromEntries(response.headers.entries()));
        
        if (!response.ok) {
            const errorText = await response.text();
            console.log('Error Response:', errorText);
            throw new Error(`Server responded with ${response.status}: ${errorText}`);
        }
        
        const data = await response.json();
        console.log('Response Data:', data);
        console.log('=====================');
        
        return {
            status: 'success',
            data: data
        };
    } catch (error) {
        console.error('=== ERROR DETAILS ===');
        console.error('Error Type:', error.name);
        console.error('Error Message:', error.message);
        console.error('Error Stack:', error.stack);
        
        // Check if it's a network error
        if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
            console.error('Network Error Details:');
            console.error('- Check if the server is running at http://localhost:8080');
            console.error('- Check if CORS is properly configured on the server');
            console.error('- Check if the server is accessible from the browser');
            console.error('- Extension Origin:', chrome.runtime.id);
        }
        
        console.error('===================');
        
        return {
            status: 'error',
            message: `Failed to connect to server: ${error.message}. Please ensure the server is running at http://localhost:8080`
        };
    }
}

// Listen for messages from the background script
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === 'getHtmlContent') {
        console.log('=== GETTING HTML CONTENT ===');
        const html = getPageHtml();
        console.log('Sending HTML content to background script');
        console.log('=====================');
        sendResponse({ html: html });
    }
    return true; // Will respond asynchronously
}); 