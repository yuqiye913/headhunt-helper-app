// Content script for web page HTML extraction
console.log('Content script loaded!');

// Function to capture page HTML with URL and tags
function getPageHtml() {
  const html = document.documentElement.outerHTML;
  const url = window.location.href;
  const title = document.title;
  
  console.log('=== HTML CONTENT CAPTURE ===');
  console.log('URL to inject:', url);
  console.log('Title:', title);
  console.log('HTML Content Length:', html.length);
  
  const urlPrefix = `<!-- EXTRACTED_URL: ${url} -->\n`;
  console.log('URL prefix to inject:', urlPrefix);
  
  const fullContent = urlPrefix + html;
  console.log('First 200 chars of final content:', fullContent.substring(0, 200));
  
  return {
    html: fullContent,
    url,
    title,
    timestamp: new Date().toISOString()
  };
}

// Function to send data to server
async function sendToServer(data) {
  try {
    const extensionId = chrome.runtime.id;
    const extensionOrigin = `chrome-extension://${extensionId}`;
    
    console.log('=== SENDING TO SERVER ===');
    console.log('URL: http://localhost:8080/api/applications/html');
    console.log('Method: POST');
    console.log('Origin:', extensionOrigin);
    
    const response = await fetch('http://localhost:8080/api/applications/html', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Origin': extensionOrigin
      },
      body: JSON.stringify(data)
    });

    console.log('=== SERVER RESPONSE ===');
    console.log('Status:', response.status, response.statusText);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Server Error:', errorText);
      throw new Error(`Server error ${response.status}: ${errorText}`);
    }

    const responseData = await response.json();
    console.log('Response Data:', responseData);
    
    return { status: 'success', data: responseData };
    
  } catch (error) {
    console.error('=== REQUEST ERROR ===');
    console.error('Type:', error.name);
    console.error('Message:', error.message);
    
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network issue - check if server is running on localhost:8080');
    }
    
    return {
      status: 'error',
      message: `Server connection failed: ${error.message}`
    };
  }
}

// Listen for requests from popup
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === 'extractJobInfo') {
    console.log('=== CAPTURING PAGE CONTENT ===');
    
    const data = getPageHtml();
    sendToServer(data).then(sendResponse);
    
    return true; // Keep the message channel open for async response
  }
});