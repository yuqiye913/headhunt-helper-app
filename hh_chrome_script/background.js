// Add initial log to confirm background script is loaded
console.log('Background script loaded!');

// Listen for messages from the popup
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    console.log('=== MESSAGE RECEIVED IN BACKGROUND ===');
    console.log('Request:', request);
    console.log('Sender:', sender);
    
    if (request.action === 'extractJobInfo') {
        console.log('Processing extractJobInfo request');
        
        // Get the active tab
        chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
            console.log('Active tab:', tabs[0]);
            
            if (tabs[0]) {
                console.log('Sending getHtmlContent message to content script');
                
                // First get the HTML content from the content script
                chrome.tabs.sendMessage(tabs[0].id, {action: 'getHtmlContent'}, async function(htmlResponse) {
                    if (chrome.runtime.lastError) {
                        console.error('Error getting HTML:', chrome.runtime.lastError);
                        sendResponse({
                            status: 'error',
                            message: 'Failed to get page content. Please refresh and try again.'
                        });
                        return;
                    }

                    try {
                        console.log('Received HTML content from page');
                        console.log('HTML length:', htmlResponse.html.length);
                        
                        // Get the extension's ID for the origin
                        const extensionId = chrome.runtime.id;
                        const extensionOrigin = `chrome-extension://${extensionId}`;
                        
                        console.log('=== MAKING SERVER REQUEST ===');
                        console.log('Extension Origin:', extensionOrigin);
                        
                        // Create JSON payload
                        const payload = {
                            htmlContent: htmlResponse.html
                        };
                        
                        console.log('Request payload size:', JSON.stringify(payload).length);
                        
                        // Make the request to the server from the background script
                        const response = await fetch('http://localhost:8080/api/applications/html', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Accept': 'application/json',
                                'Origin': extensionOrigin
                            },
                            body: JSON.stringify(payload)
                        });

                        console.log('=== SERVER RESPONSE ===');
                        console.log('Status:', response.status);
                        console.log('Status Text:', response.statusText);
                        console.log('Headers:', Object.fromEntries(response.headers.entries()));

                        if (!response.ok) {
                            const errorText = await response.text();
                            console.error('Error response:', errorText);
                            throw new Error(`Server responded with ${response.status}: ${errorText}`);
                        }

                        const data = await response.json();
                        console.log('Response data:', data);
                        
                        console.log('Sending success response to popup');
                        sendResponse({
                            status: 'success',
                            data: data
                        });
                    } catch (error) {
                        console.error('=== ERROR IN BACKGROUND SCRIPT ===');
                        console.error('Error type:', error.name);
                        console.error('Error message:', error.message);
                        console.error('Error stack:', error.stack);
                        
                        sendResponse({
                            status: 'error',
                            message: error.message || 'Failed to process job information'
                        });
                    }
                });
            } else {
                console.error('No active tab found');
                sendResponse({
                    status: 'error',
                    message: 'No active tab found'
                });
            }
        });
        return true; // Will respond asynchronously
    }
}); 