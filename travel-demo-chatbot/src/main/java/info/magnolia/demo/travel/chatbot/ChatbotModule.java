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
package info.magnolia.demo.travel.chatbot;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration bean for the travel-demo-chatbot module.
 */
public class ChatbotModule {

    private String model = "gemini-3-flash-preview";

    private String systemPromptTemplate = "You are a travel advisor for the Magnolia Travel demo site.\n\n"
            + "Always reply in ${language}. If the visitor writes in a different language,\n"
            + "still reply in ${language} unless they explicitly ask you to switch.\n\n"
            + "You only answer questions about travel, tours, and destinations available\n"
            + "on this site. If asked about anything else, politely redirect.\n\n"
            + "Use the available tools to ground your recommendations in real content.\n"
            + "Prefer one or two strong suggestions over long lists. When you reference\n"
            + "a tour or destination, include its title.\n\n"
            + "Visitor profile (for tailoring; do not mention these traits explicitly\n"
            + "unless asked): ${visitorTraits}";

    private List<String> enabledTools = new ArrayList<>(List.of("tours", "destinations", "editorial"));

    private int maxToolIterations = 5;

    private int historyTurnLimit = 20;

    private int requestTimeoutMs = 30000;

    private int rateLimitPerMinute = 30;

    private int maxTokensPerSession = 50000;

    private int maxUserMessageChars = 4000;

    private String apiKey;

    public String getApiKey() {
        if (apiKey == null) {
            java.util.Map<String,String> merged = info.magnolia.demo.travel.chatbot.env.EnvLoader
                    .merge(info.magnolia.demo.travel.chatbot.env.EnvLoader.loadFile(new java.io.File(".env")),
                           System.getenv());
            apiKey = merged.get("GEMINI_API_KEY");
        }
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPromptTemplate() {
        return systemPromptTemplate;
    }

    public void setSystemPromptTemplate(String systemPromptTemplate) {
        this.systemPromptTemplate = systemPromptTemplate;
    }

    public List<String> getEnabledTools() {
        return enabledTools;
    }

    public void setEnabledTools(List<String> enabledTools) {
        this.enabledTools = enabledTools;
    }

    public int getMaxToolIterations() {
        return maxToolIterations;
    }

    public void setMaxToolIterations(int maxToolIterations) {
        this.maxToolIterations = maxToolIterations;
    }

    public int getHistoryTurnLimit() {
        return historyTurnLimit;
    }

    public void setHistoryTurnLimit(int historyTurnLimit) {
        this.historyTurnLimit = historyTurnLimit;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public int getMaxTokensPerSession() {
        return maxTokensPerSession;
    }

    public void setMaxTokensPerSession(int maxTokensPerSession) {
        this.maxTokensPerSession = maxTokensPerSession;
    }

    public int getMaxUserMessageChars() {
        return maxUserMessageChars;
    }

    public void setMaxUserMessageChars(int maxUserMessageChars) {
        this.maxUserMessageChars = maxUserMessageChars;
    }
}
