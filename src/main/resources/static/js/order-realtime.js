document.addEventListener('DOMContentLoaded', function () {
    if (window.__orderWsInitialized) {
        return;
    }
    window.__orderWsInitialized = true;
    const STOMP = window.StompJs || window.Stomp;
    if (!window.SockJS || !STOMP || typeof STOMP.Client !== 'function') {
        return;
    }

    function updateOrderStatus(payload) {
        if (!payload || !payload.orderId) {
            return;
        }
        const orderId = String(payload.orderId);
        const label = payload.statusLabel || payload.status || '';
        const targets = document.querySelectorAll('[data-order-status-badge="true"][data-order-id="' + orderId + '"]');
        targets.forEach(function (target) {
            target.textContent = label;
        });
        window.dispatchEvent(new CustomEvent('order-status-realtime', {
            detail: {
                orderId: orderId,
                status: payload.status || '',
                statusLabel: label
            }
        }));
    }

    const client = new STOMP.Client({
        webSocketFactory: function () {
            return new SockJS('/ws');
        },
        reconnectDelay: 5000
    });

    client.onConnect = function () {
        client.subscribe('/topic/orders', function (frame) {
            let payload = null;
            try {
                payload = JSON.parse(frame.body || '{}');
            } catch (e) {
                return;
            }
            updateOrderStatus(payload);
        });
    };

    client.activate();
});
