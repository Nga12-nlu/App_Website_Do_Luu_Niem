(function () {
    'use strict';

    function formatVnd(n) {
        return new Intl.NumberFormat('vi-VN').format(n);
    }

    function initRevenueChart() {
        var el = document.getElementById('revenueChart');
        var dataEl = document.getElementById('revenueChartData');
        if (!el || !dataEl || typeof Chart === 'undefined') return;

        var points = [];
        try {
            points = JSON.parse(dataEl.textContent);
        } catch (e) {
            return;
        }

        var labels = points.map(function (p) { return p.label; });
        var revenues = points.map(function (p) { return Number(p.revenue) || 0; });
        var orders = points.map(function (p) { return Number(p.orders) || 0; });

        new Chart(el, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Doanh thu (đ)',
                        data: revenues,
                        borderColor: '#3498db',
                        backgroundColor: 'rgba(52, 152, 219, 0.12)',
                        fill: true,
                        tension: 0.35,
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Số đơn',
                        data: orders,
                        borderColor: '#27ae60',
                        backgroundColor: 'transparent',
                        borderDash: [4, 4],
                        tension: 0.35,
                        pointRadius: 3,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'top', labels: { usePointStyle: true, padding: 16 } },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                if (ctx.datasetIndex === 0) {
                                    return 'Doanh thu: ' + formatVnd(ctx.parsed.y) + ' đ';
                                }
                                return 'Đơn: ' + ctx.parsed.y;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        position: 'left',
                        ticks: {
                            callback: function (v) { return formatVnd(v); }
                        },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    y1: {
                        position: 'right',
                        grid: { drawOnChartArea: false },
                        ticks: { stepSize: 1 }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    function initOrderStatusChart() {
        var el = document.getElementById('orderStatusChart');
        var stats = window.dashboardOrderStats;
        if (!el || !stats || typeof Chart === 'undefined') return;

        var values = [
            stats.pending || 0,
            stats.confirmed || 0,
            stats.shipped || 0,
            stats.cancelled || 0
        ];
        var total = values.reduce(function (a, b) { return a + b; }, 0);

        if (total === 0) {
            values = [1];
            new Chart(el, {
                type: 'doughnut',
                data: {
                    labels: ['Chưa có đơn'],
                    datasets: [{
                        data: values,
                        backgroundColor: ['#dee2e6'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    cutout: '65%',
                    plugins: { legend: { display: false } }
                }
            });
            return;
        }

        new Chart(el, {
            type: 'doughnut',
            data: {
                labels: ['Chờ xử lý', 'Đã xác nhận', 'Đã giao', 'Đã hủy'],
                datasets: [{
                    data: values,
                    backgroundColor: ['#f39c12', '#27ae60', '#17a2b8', '#e74c3c'],
                    borderWidth: 2,
                    borderColor: '#fff',
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                cutout: '65%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                var pct = Math.round((ctx.parsed / total) * 100);
                                return ctx.label + ': ' + ctx.parsed + ' (' + pct + '%)';
                            }
                        }
                    }
                }
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initRevenueChart();
        initOrderStatusChart();
    });
})();
