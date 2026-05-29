(function () {
    'use strict';

    var cfg = window.CHECKOUT_CONFIG || {};
    var base = cfg.contextPath || '';
    var providerSelect = document.getElementById('addressProvider');
    var provinceSelect = document.getElementById('province');
    var districtSelect = document.getElementById('district');
    var wardSelect = document.getElementById('ward');
    var form = document.getElementById('checkoutForm');

    function fmt(n) {
        return new Intl.NumberFormat('vi-VN').format(Number(n) || 0);
    }

    function setLoading(select, msg) {
        select.innerHTML = '<option value="">' + (msg || 'Đang tải...') + '</option>';
        select.disabled = true;
    }

    function fillSelect(select, items, placeholder, selectedCode) {
        select.innerHTML = '<option value="">' + placeholder + '</option>';
        (items || []).forEach(function (p) {
            var opt = document.createElement('option');
            opt.value = p.code;
            opt.textContent = p.name;
            opt.dataset.name = p.name;
            if (selectedCode && String(p.code) === String(selectedCode)) {
                opt.selected = true;
            }
            select.appendChild(opt);
        });
        select.disabled = false;
    }

    function apiGet(url) {
        return fetch(url, { credentials: 'same-origin' }).then(function (r) {
            return r.json().then(function (data) {
                if (!r.ok || data.success === false) {
                    throw new Error(data.message || 'Lỗi tải dữ liệu');
                }
                return data;
            });
        });
    }

    function provider() {
        return providerSelect ? providerSelect.value : 'open-api';
    }

    function syncHidden(selectEl, codeId, nameId) {
        var codeEl = document.getElementById(codeId);
        var nameEl = document.getElementById(nameId);
        var opt = selectEl.options[selectEl.selectedIndex];
        if (opt && opt.value) {
            codeEl.value = opt.value;
            nameEl.value = opt.dataset.name || opt.textContent;
        } else {
            codeEl.value = '';
            nameEl.value = '';
        }
    }

    function updateAddressPreview() {
        var detail = document.getElementById('addressDetail').value.trim();
        var parts = [detail];
        ['wardName', 'districtName', 'provinceName'].forEach(function (id) {
            var v = document.getElementById(id).value;
            if (v) parts.push(v);
        });
        var preview = document.getElementById('addressPreview');
        var text = document.getElementById('addressPreviewText');
        var full = parts.filter(Boolean).join(', ');
        if (full.length > 5) {
            text.textContent = full;
            preview.style.display = 'block';
        } else {
            preview.style.display = 'none';
        }
    }

    function updateQuoteDisplay(quote) {
        function set(id, val) {
            var el = document.getElementById(id);
            if (!el) return;
            el.setAttribute('data-price', val);
            el.textContent = fmt(val);
        }
        set('sumSubtotal', quote.subtotal);
        set('sumDiscount', quote.discountAmount);
        set('sumShipping', quote.shippingFee);
        set('sumTotal', quote.totalAmount);
        var row = document.getElementById('rowDiscount');
        if (row) {
            row.style.display = Number(quote.discountAmount) > 0 ? '' : 'none';
        }
        var msg = document.getElementById('couponMessage');
        if (msg && quote.couponMessage) {
            msg.textContent = quote.couponMessage;
            msg.className = 'form-text mt-1 ' + (quote.couponApplied ? 'text-success' : 'text-danger');
        }
        var removeBtn = document.getElementById('btnRemoveCoupon');
        if (removeBtn) {
            removeBtn.style.display = quote.couponApplied ? '' : 'none';
        }
        if (quote.couponApplied && quote.couponCode) {
            document.getElementById('couponCodeInput').value = quote.couponCode;
        }
    }

    function refreshQuote() {
        var provinceCode = document.getElementById('provinceCode').value;
        var url = base + '/api/checkout/quote';
        if (provinceCode) {
            url += '?provinceCode=' + encodeURIComponent(provinceCode);
        }
        return apiGet(url).then(function (data) {
            updateQuoteDisplay(data.quote);
        }).catch(function () { /* ignore */ });
    }

    function loadProvinces(selectedCode) {
        setLoading(provinceSelect, 'Đang tải tỉnh/thành...');
        districtSelect.innerHTML = '<option value="">— Chọn tỉnh trước —</option>';
        districtSelect.disabled = true;
        wardSelect.innerHTML = '<option value="">— Chọn quận trước —</option>';
        wardSelect.disabled = true;
        return apiGet(base + '/api/address/provinces?provider=' + encodeURIComponent(provider()))
            .then(function (data) {
                fillSelect(provinceSelect, data.items, '— Chọn tỉnh/thành —', selectedCode || cfg.formProvinceCode);
                if (provinceSelect.value) {
                    syncHidden(provinceSelect, 'provinceCode', 'provinceName');
                    return loadDistricts(cfg.formDistrictCode);
                }
            })
            .catch(function (e) {
                provinceSelect.innerHTML = '<option value="">Lỗi: thử đổi nguồn API</option>';
                console.error(e);
            });
    }

    function loadDistricts(selectedCode) {
        var pcode = document.getElementById('provinceCode').value;
        if (!pcode) return Promise.resolve();
        setLoading(districtSelect, 'Đang tải quận/huyện...');
        wardSelect.innerHTML = '<option value="">— Chọn quận trước —</option>';
        wardSelect.disabled = true;
        return apiGet(base + '/api/address/districts?provider=' + encodeURIComponent(provider()) +
            '&province=' + encodeURIComponent(pcode))
            .then(function (data) {
                fillSelect(districtSelect, data.items, '— Chọn quận/huyện —', selectedCode || cfg.formDistrictCode);
                if (districtSelect.value) {
                    syncHidden(districtSelect, 'districtCode', 'districtName');
                    return loadWards(cfg.formWardCode);
                }
            })
            .catch(function (e) {
                districtSelect.innerHTML = '<option value="">Không tải được quận/huyện</option>';
                console.error(e);
            });
    }

    function loadWards(selectedCode) {
        var dcode = document.getElementById('districtCode').value;
        if (!dcode) return Promise.resolve();
        setLoading(wardSelect, 'Đang tải phường/xã...');
        return apiGet(base + '/api/address/wards?provider=' + encodeURIComponent(provider()) +
            '&district=' + encodeURIComponent(dcode))
            .then(function (data) {
                fillSelect(wardSelect, data.items, '— Chọn phường/xã —', selectedCode || cfg.formWardCode);
                syncHidden(wardSelect, 'wardCode', 'wardName');
                updateAddressPreview();
                return refreshQuote();
            })
            .catch(function (e) {
                wardSelect.innerHTML = '<option value="">Không tải được phường/xã</option>';
                console.error(e);
            });
    }

    if (providerSelect) {
        providerSelect.value = cfg.defaultProvider || 'open-api';
        providerSelect.addEventListener('change', function () {
            cfg.formProvinceCode = '';
            cfg.formDistrictCode = '';
            cfg.formWardCode = '';
            loadProvinces();
        });
    }

    if (provinceSelect) {
        provinceSelect.addEventListener('change', function () {
            syncHidden(provinceSelect, 'provinceCode', 'provinceName');
            document.getElementById('districtCode').value = '';
            document.getElementById('districtName').value = '';
            document.getElementById('wardCode').value = '';
            document.getElementById('wardName').value = '';
            loadDistricts();
            updateAddressPreview();
            refreshQuote();
        });
    }

    if (districtSelect) {
        districtSelect.addEventListener('change', function () {
            syncHidden(districtSelect, 'districtCode', 'districtName');
            document.getElementById('wardCode').value = '';
            document.getElementById('wardName').value = '';
            loadWards();
            updateAddressPreview();
        });
    }

    if (wardSelect) {
        wardSelect.addEventListener('change', function () {
            syncHidden(wardSelect, 'wardCode', 'wardName');
            updateAddressPreview();
            refreshQuote();
        });
    }

    document.getElementById('addressDetail').addEventListener('input', updateAddressPreview);

    document.getElementById('btnApplyCoupon').addEventListener('click', function () {
        var code = document.getElementById('couponCodeInput').value.trim();
        var provinceCode = document.getElementById('provinceCode').value;
        var body = new URLSearchParams();
        body.set('code', code);
        if (provinceCode) body.set('provinceCode', provinceCode);
        fetch(base + '/api/coupon/apply', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString()
        })
            .then(function (r) { return r.json().then(function (d) { return { ok: r.ok, data: d }; }); })
            .then(function (res) {
                var msg = document.getElementById('couponMessage');
                if (!res.ok) {
                    msg.textContent = res.data.message || 'Mã không hợp lệ';
                    msg.className = 'form-text mt-1 text-danger';
                    return;
                }
                updateQuoteDisplay(res.data.quote);
                msg.textContent = res.data.message || '';
                msg.className = 'form-text mt-1 text-success';
                document.getElementById('btnRemoveCoupon').style.display = res.data.quote.couponApplied ? '' : 'none';
            });
    });

    document.getElementById('btnRemoveCoupon').addEventListener('click', function () {
        var provinceCode = document.getElementById('provinceCode').value;
        var body = new URLSearchParams();
        if (provinceCode) body.set('provinceCode', provinceCode);
        fetch(base + '/api/coupon/remove', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString()
        })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.quote) updateQuoteDisplay(data.quote);
                document.getElementById('couponCodeInput').value = '';
                document.getElementById('couponMessage').textContent = '';
                document.getElementById('btnRemoveCoupon').style.display = 'none';
            });
    });

    if (form) {
        form.addEventListener('submit', function (e) {
            syncHidden(provinceSelect, 'provinceCode', 'provinceName');
            syncHidden(districtSelect, 'districtCode', 'districtName');
            syncHidden(wardSelect, 'wardCode', 'wardName');
            if (!form.checkValidity()) {
                e.preventDefault();
                form.classList.add('was-validated');
                return;
            }
            if (!document.getElementById('provinceCode').value ||
                !document.getElementById('districtCode').value ||
                !document.getElementById('wardCode').value) {
                e.preventDefault();
                alert('Vui lòng chọn đầy đủ Tỉnh, Quận/Huyện và Phường/Xã.');
            }
        });
    }

    loadProvinces();
})();
