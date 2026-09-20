import http.server, os
os.chdir(r'ecom-backend/frontend')
handler = http.server.SimpleHTTPRequestHandler
with http.server.HTTPServer(('127.0.0.1', 3000), handler) as srv:
    print('Frontend server running at http://localhost:3000', flush=True)
    srv.serve_forever()
