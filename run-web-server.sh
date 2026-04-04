#!/bin/bash
# Simple HTTP server for testing Word Clock web app

echo "🚀 Starting Word Clock Web Server..."
echo "📱 Open in browser: http://localhost:8000/web-clock.html"
echo "📱 Or on mobile: http://YOUR_IP:8000/web-clock.html"
echo ""
echo "To add to home screen on iPhone:"
echo "1. Open in Safari"
echo "2. Tap Share button"
echo "3. Select 'Add to Home Screen'"
echo "4. Tap 'Add'"
echo ""

cd "$(dirname "$0")"
python3 -m http.server 8000