document.addEventListener('DOMContentLoaded', function () {
    const dropdown = document.querySelector('.notification-dropdown');
    if (!dropdown || window.__notificationWsInitialized) {
        return;
    }
    window.__notificationWsInitialized = true;

    const menu = dropdown.querySelector('.notification-menu');
    const button = dropdown.querySelector('.notification-btn');
    const VUE = window.Vue;
    if (!menu || !button || !VUE || typeof VUE.createApp !== 'function') {
        return;
    }

    const initialNotifications = Array.from(menu.querySelectorAll('.notification-item')).map(function (item) {
        return {
            id: item.dataset.notificationId ? Number(item.dataset.notificationId) : null,
            title: (item.querySelector('.notification-title') || {}).textContent || 'Thông báo',
            content: (item.querySelector('.notification-content') || {}).textContent || '',
            link: item.getAttribute('href') || '/home/index',
            read: !item.classList.contains('unread')
        };
    });
    const badgeEl = button.querySelector('.notification-badge');
    const initialUnread = badgeEl ? Number(badgeEl.textContent || '0') : 0;

    const vueMenu = VUE.createApp({
        data: function () {
            return {
                notifications: initialNotifications,
                unreadCount: Number.isFinite(initialUnread) ? initialUnread : 0
            };
        },
        methods: {
            upsertNotification: function (payload) {
                const next = {
                    id: payload && payload.id ? Number(payload.id) : null,
                    title: payload && payload.title ? payload.title : 'Thông báo',
                    content: payload && payload.content ? payload.content : '',
                    link: payload && payload.link ? payload.link : '/home/index',
                    read: !!(payload && payload.read)
                };
                if (next.id != null) {
                    this.notifications = this.notifications.filter(function (item) {
                        return item.id !== next.id;
                    });
                }
                this.notifications.unshift(next);
                if (this.notifications.length > 100) {
                    this.notifications = this.notifications.slice(0, 100);
                }
            }
        },
        watch: {
            unreadCount: function (value) {
                let badge = button.querySelector('.notification-badge');
                if (!value || value <= 0) {
                    if (badge) {
                        badge.remove();
                    }
                    return;
                }
                if (!badge) {
                    badge = document.createElement('span');
                    badge.className = 'notification-badge';
                    button.appendChild(badge);
                }
                badge.textContent = String(value);
            }
        },
        template: `
            <div>
                <div class="notification-header">Thông báo</div>
                <div class="notification-empty" v-if="notifications.length === 0">Không có thông báo mới</div>
                <a
                    class="notification-item"
                    v-for="notification in notifications"
                    :key="notification.id || (notification.title + '_' + notification.content)"
                    :class="{ unread: !notification.read }"
                    :href="notification.link || '/home/index'"
                    :data-notification-id="notification.id || null"
                >
                    <span class="notification-dot" v-if="!notification.read"></span>
                    <div class="notification-body">
                        <div class="notification-title">{{ notification.title }}</div>
                        <div class="notification-content">{{ notification.content }}</div>
                    </div>
                </a>
            </div>
        `
    }).mount(menu);

    const STOMP = window.StompJs || window.Stomp;
    if (!window.SockJS || !STOMP || typeof STOMP.Client !== 'function') {
        return;
    }
    const client = new STOMP.Client({
        webSocketFactory: function () {
            return new SockJS('/ws');
        },
        reconnectDelay: 5000
    });

    client.onConnect = function () {
        client.subscribe('/user/queue/notifications', function (frame) {
            let payload = null;
            try {
                payload = JSON.parse(frame.body || '{}');
            } catch (e) {
                return;
            }
            if (!payload) {
                return;
            }
            if (vueMenu) {
                vueMenu.upsertNotification(payload);
                vueMenu.unreadCount = payload.unreadCount || 0;
            }
        });
    };

    client.activate();
});
