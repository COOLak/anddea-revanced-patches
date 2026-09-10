/* Copyright (C) 2026 COOLak. Licensed under GPL-3.0-only. */
package app.morphe.extension.youtube.patches.voiceovertranslation;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ephemeral YouTube player credentials; never sent to media hosts or Yandex. */
final class VotPlayerRequestContext {
    private record Entry(Map<String, String> headers, long expiresAt) { }
    private static final Map<String, Entry> cache = new LinkedHashMap<>();

    static synchronized String put(String url, Map<String, String> headers) {
        URI uri = URI.create(url);
        if (!"https".equals(uri.getScheme()) || !isYouTubeHost(uri.getHost())
                || !"/youtubei/v1/player".equals(uri.getPath()) || uri.getRawQuery() == null) return null;
        String videoId = null;
        for (String field : uri.getRawQuery().split("&")) {
            if (field.startsWith("id=")) videoId = field.substring(3);
        }
        if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}")) return null;
        Map<String, String> copy = new LinkedHashMap<>();
        for (var header : headers.entrySet()) {
            if ("Authorization".equalsIgnoreCase(header.getKey())) copy.put("Authorization", header.getValue());
            if ("X-Goog-PageId".equalsIgnoreCase(header.getKey())) copy.put("X-Goog-PageId", header.getValue());
        }
        if (!copy.containsKey("Authorization")) return null;
        cache.remove(videoId);
        cache.put(videoId, new Entry(Map.copyOf(copy), System.currentTimeMillis() + 5 * 60_000));
        while (cache.size() > 8) cache.remove(cache.keySet().iterator().next());
        return videoId;
    }

    static synchronized Map<String, String> get(String videoId) {
        Entry entry = cache.get(videoId);
        if (entry == null) return Map.of();
        if (System.currentTimeMillis() >= entry.expiresAt()) {
            cache.remove(videoId);
            return Map.of();
        }
        return entry.headers();
    }

    private static boolean isYouTubeHost(String host) {
        return host != null && (host.equals("youtubei.googleapis.com") || host.equals("youtube.com") || host.endsWith(".youtube.com"));
    }
}
