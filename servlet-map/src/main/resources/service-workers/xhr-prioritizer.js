const MAX_CONCURRENT_REQUESTS = 6;
const LIMIT_CONCURRENT_REQUESTS = 2;

const LOW_PRIO_REQUESTS = ['GetLayerTile', 'GetWFSFeatures', 'GetWFSVectorTile'];
const lowPrioTester = new RegExp('^.*(\\/action\\?action_route=)(' + LOW_PRIO_REQUESTS.join('|') + ')\\&.*$');

let pendingHighPrioRequestCount = 0;
let pendingLowPrioRequestCount = 0;
let lowPrioQueue = [];

const requestNextFromQueue = () => {
    if (lowPrioQueue.length === 0) {
        return;
    }
    const pendingLimit = pendingHighPrioRequestCount >= MAX_CONCURRENT_REQUESTS ? LIMIT_CONCURRENT_REQUESTS : MAX_CONCURRENT_REQUESTS;
    if (pendingLowPrioRequestCount >= pendingLimit) {
        return;
    }
    let unusedLimit = pendingLimit - pendingLowPrioRequestCount;
    while (unusedLimit !== 0 && lowPrioQueue.length !== 0) {
        fetchLowPrioFromServer();
        unusedLimit--;
    }
};

const debugLog = () => {
    console.debug(
        'Pending high: ' + pendingHighPrioRequestCount +
        ', low: ' + pendingLowPrioRequestCount +
        ', queued: ' + lowPrioQueue.length);
};

const fetchLowPrioFromServer = () => {
    const { event, resolveHandle: resolve } = lowPrioQueue.shift();
    pendingLowPrioRequestCount++;
    debugLog();
    const response = fetch(event.request)
        .finally(() => {
            pendingLowPrioRequestCount--;
            requestNextFromQueue();
        });
    resolve(response);
};

self.addEventListener('fetch', event => {
    if (!lowPrioTester.test(event.request.url)) {
        pendingHighPrioRequestCount++;
        debugLog();
        const response = fetch(event.request).finally(() => {
            pendingHighPrioRequestCount--;
        });
        event.respondWith(response);
        return;
    }
    let resolveHandle;
    const promise = new Promise((resolve, reject) => {
        resolveHandle = resolve;
    });
    lowPrioQueue.push({ event, resolveHandle });
    requestNextFromQueue();
    event.respondWith(promise);
});

self.addEventListener('activate', event => {
    pendingHighPrioRequestCount = 0;
    pendingLowPrioRequestCount = 0;
    lowPrioQueue = [];
});
