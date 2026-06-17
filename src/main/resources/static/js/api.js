/**
 * API 请求工具 — 封装 fetch，自动解析 ApiResponse
 */
const api = {
    async get(url) {
        const resp = await fetch(url);
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const json = await resp.json();
        if (json.code && json.code !== 200) throw new Error(json.message || '请求失败');
        return json.data;
    },

    async post(url, body) {
        const resp = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: body ? JSON.stringify(body) : undefined
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const json = await resp.json();
        if (json.code && json.code !== 200) throw new Error(json.message || '请求失败');
        return json.data;
    }
};
