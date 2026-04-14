/**
 * This file Copyright (c) 2026 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.chatbot.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GeminiClient {

    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private static final Sleeper DEFAULT_SLEEPER = Thread::sleep;

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final long[] BACKOFF_MS = { 250, 1000, 4000 };

    private final String apiKey;
    private final String baseUrl;
    private final int timeoutMs;
    private final Sleeper sleeper;
    private final HttpClient http;

    public GeminiClient(String apiKey, String baseUrl, int timeoutMs, Sleeper sleeper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.sleeper = sleeper;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
    }

    public JsonNode generate(String model, ObjectNode body) throws LlmException {
        String url = baseUrl + "/models/" + model + ":generateContent";
        byte[] payload;
        try {
            payload = JSON.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new LlmException(0, "serialization failed", e);
        }

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        int attempt = 0;
        while (true) {
            try {
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                int status = resp.statusCode();
                if (status >= 200 && status < 300) {
                    return JSON.readTree(resp.body());
                }
                if (isRetryable(status) && attempt < BACKOFF_MS.length) {
                    long wait = BACKOFF_MS[attempt] + ThreadLocalRandom.current().nextLong(0, 100);
                    log.warn("Gemini {} on attempt {}, retrying in {}ms", status, attempt + 1, wait);
                    sleeper.sleep(wait);
                    attempt++;
                    continue;
                }
                throw new LlmException(status, "Gemini call failed: HTTP " + status);
            } catch (IOException | InterruptedException e) {
                if (attempt < BACKOFF_MS.length) {
                    try {
                        sleeper.sleep(BACKOFF_MS[attempt]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LlmException(0, "interrupted", ie);
                    }
                    attempt++;
                    continue;
                }
                throw new LlmException(0, "transport error: " + e.getMessage(), e);
            }
        }
    }

    private static boolean isRetryable(int s) {
        return s == 429 || (s >= 500 && s < 600);
    }
}
