var DEBUG = true;
var MAX_CONCURRENT_REQUESTS = 6;
var LIMIT_CONCURRENT_REQUESTS = 2;
var PRIO_TRAFFIC_LIMIT = 3;

var LOW_PRIO_REQUESTS = ['GetLayerTile', 'GetWFSFeatures', 'GetWFSVectorTile'];
var lowPrioTester = new RegExp('^.*(\\/action\\?action_route=)(' + LOW_PRIO_REQUESTS.join('|') + ')\\&.*$');

var pendingHighPrioRequestCount = 0;
var pendingLowPrioRequestCount = 0;
var lowPrioQueue = [];

function requestNextFromQueue () {
    if (lowPrioQueue.length === 0) {
        return;
    }
    var pendingLimit = pendingHighPrioRequestCount >= PRIO_TRAFFIC_LIMIT ? LIMIT_CONCURRENT_REQUESTS : MAX_CONCURRENT_REQUESTS;
    if (pendingLowPrioRequestCount >= pendingLimit) {
        return;
    }
    var unusedLimit = pendingLimit - pendingLowPrioRequestCount;
    while (unusedLimit !== 0 && lowPrioQueue.length !== 0) {
        fetchLowPrioFromServer();
        unusedLimit--;
    }
};

function debugLog () {
    if (!DEBUG) {
        return;
    }
    console.debug(
        'Pending high: ' + pendingHighPrioRequestCount +
        ', low: ' + pendingLowPrioRequestCount +
        ', queued: ' + lowPrioQueue.length);
};

function fetchLowPrioFromServer () {
    var entry = lowPrioQueue.shift();
    var event = entry.event;
    var resolve = entry.resolve;
    pendingLowPrioRequestCount++;
    resolve(fetch(event.request).then(lowPrioFetchDone, lowPrioFetchDone));
    debugLog();
};

function lowPrioFetchDone (response) {
    pendingLowPrioRequestCount--;
    requestNextFromQueue();
    return response;
};

function decreaseHighPrioPendingCount (response) {
    pendingHighPrioRequestCount--;
    return response;
};

self.addEventListener('fetch', function (event) {
    if (!lowPrioTester.test(event.request.url)) {
        pendingHighPrioRequestCount++;
        event.respondWith(fetch(event.request).then(decreaseHighPrioPendingCount, decreaseHighPrioPendingCount));
        debugLog();
        return;
    }
    var resolveHandle;
    var promise = new Promise(function (resolve, reject) {
        resolveHandle = resolve;
    });
    lowPrioQueue.push({ event: event, resolve: resolveHandle });
    requestNextFromQueue();
    event.respondWith(promise);
});

self.addEventListener('activate', function () {
    pendingHighPrioRequestCount = 0;
    pendingLowPrioRequestCount = 0;
    lowPrioQueue = [];
});
