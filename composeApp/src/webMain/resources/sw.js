const CACHE_PREFIX = 'ubaa-cache-';

self.addEventListener('install', (event) => {
    event.waitUntil(self.skipWaiting());
});

self.addEventListener('activate', (event) => {
    event.waitUntil((async () => {
        const cacheNames = await caches.keys();
        await Promise.all(
            cacheNames
                .filter((cacheName) => cacheName.startsWith(CACHE_PREFIX))
                .map((cacheName) => caches.delete(cacheName))
        );

        await self.clients.claim();
        await self.registration.unregister();

        const clients = await self.clients.matchAll({
            type: 'window',
            includeUncontrolled: true
        });

        await Promise.all(
            clients.map((client) =>
                client.navigate(client.url).catch(() => null)
            )
        );
    })());
});
