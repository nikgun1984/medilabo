const express = require('express');
const path = require('path');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 3000;
// Inside Docker the gateway is reachable by service name; outside use localhost
const GATEWAY_HOST = process.env.GATEWAY_HOST || 'gateway';
const GATEWAY_PORT = process.env.GATEWAY_PORT || 8080;

// Helper: proxy a GET request to the gateway and forward the response
function proxyGet(targetPath, req, res) {
    const options = {
        hostname: GATEWAY_HOST,
        port: GATEWAY_PORT,
        path: targetPath,
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    };
    const proxyReq = http.request(options, (proxyRes) => {
        res.status(proxyRes.statusCode);
        res.set('Content-Type', 'application/json');
        proxyRes.pipe(res);
    });
    proxyReq.on('error', () => res.status(503).json({ status: 'DOWN', error: 'unreachable' }));
    proxyReq.end();
}

// Serve static files
app.use(express.static(path.join(__dirname, '../public')));

// Frontend health check
app.get('/health', (req, res) => {
    res.json({ status: 'UP', service: 'frontend' });
});

// Server-side proxy health checks — avoids any browser CORS issues
app.get('/proxy/health/gateway',      (req, res) => proxyGet('/actuator/health',          req, res));
app.get('/proxy/health/demographics', (req, res) => proxyGet('/api/demographics/health',  req, res));

// Serve the main page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.listen(PORT, () => {
    console.log(`Frontend server running on port ${PORT}`);
});
