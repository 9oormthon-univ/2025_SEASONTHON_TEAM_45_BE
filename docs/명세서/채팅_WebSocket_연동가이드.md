# CareFreePass 채팅 WebSocket 연동 가이드 💬

## 📋 개요

CareFreePass 병원 예약 시스템에서 **실시간 AI 채팅**을 위한 WebSocket 연동 방법을 설명합니다.  
환자가 증상을 입력하면 AI가 실시간으로 응답하고, 적절한 진료과를 추천해드립니다.

**WebSocket URL**: `ws://13.124.250.98:8080/ws/chat`  
**프로토콜**: STOMP over SockJS

---

## 💬 채팅 시스템 구조

### 📊 전체 플로우
```
1. 환자가 채팅 세션 시작 (REST API)
2. WebSocket 연결 및 구독
3. 실시간 메시지 송수신
4. AI 응답 및 진료과 추천
5. 채팅 세션 완료 (REST API)
```

### 🔄 메시지 흐름
```
환자 메시지 → WebSocket → 서버 → AI 처리 → WebSocket → 환자 화면
```

---

## 🚀 1. 환경 설정

### 1.1 필요한 라이브러리

#### 🌐 웹 (JavaScript)
```bash
npm install @stomp/stompjs sockjs-client
```

#### 📱 iOS (Swift)
```swift
// Podfile에 추가
pod 'Starscream', '~> 4.0'
pod 'StompClientLib'
```

#### 🤖 Android (Kotlin)
```gradle
// app/build.gradle에 추가
implementation 'org.hildan.krossbow:krossbow-stomp-core:4.3.0'
implementation 'org.hildan.krossbow:krossbow-websocket-okhttp:4.3.0'
```

---

## 🔌 2. WebSocket 연결

### 2.1 JavaScript (웹) 연결

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class ChatWebSocket {
    constructor() {
        this.client = null;
        this.sessionId = null;
        this.memberId = null;
        this.isConnected = false;
    }
    
    // WebSocket 연결
    connect(sessionId, memberId) {
        this.sessionId = sessionId;
        this.memberId = memberId;
        
        // SockJS를 사용한 STOMP 클라이언트 생성
        this.client = new Client({
            webSocketFactory: () => new SockJS('http://13.124.250.98:8080/ws/chat'),
            debug: (str) => console.log('STOMP Debug:', str),
            
            onConnect: (frame) => {
                console.log('WebSocket 연결 성공:', frame);
                this.isConnected = true;
                this.subscribeToTopic();
                
                // 연결 성공 콜백
                this.onConnected();
            },
            
            onStompError: (frame) => {
                console.error('STOMP 에러:', frame.headers['message']);
                console.error('세부 정보:', frame.body);
                this.isConnected = false;
            },
            
            onWebSocketClose: (event) => {
                console.log('WebSocket 연결 종료:', event);
                this.isConnected = false;
            }
        });
        
        // 연결 시작
        this.client.activate();
    }
    
    // 채팅방 구독
    subscribeToTopic() {
        if (this.client && this.isConnected) {
            // 특정 세션의 메시지 구독
            this.client.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
                console.log('메시지 수신:', message.body);
                const chatMessage = JSON.parse(message.body);
                this.onMessageReceived(chatMessage);
            });
            
            console.log(`채팅방 구독 완료: /topic/chat/${this.sessionId}`);
        }
    }
    
    // 메시지 전송
    sendMessage(content) {
        if (this.client && this.isConnected) {
            const message = {
                sessionId: this.sessionId,
                memberId: this.memberId,
                content: content,
                timestamp: new Date().toISOString()
            };
            
            this.client.publish({
                destination: '/app/chat.send',
                body: JSON.stringify(message)
            });
            
            console.log('메시지 전송:', message);
        } else {
            console.error('WebSocket이 연결되지 않았습니다.');
        }
    }
    
    // 연결 종료
    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.isConnected = false;
            console.log('WebSocket 연결 종료');
        }
    }
    
    // 콜백 함수들 (사용자가 구현)
    onConnected() {
        // WebSocket 연결 성공 시 호출
        console.log('채팅 연결 완료!');
    }
    
    onMessageReceived(message) {
        // 메시지 수신 시 호출
        console.log('새 메시지:', message);
        // UI 업데이트 로직 구현
        this.displayMessage(message);
    }
    
    displayMessage(message) {
        // DOM에 메시지 추가
        const messagesContainer = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');
        messageElement.className = message.sender === 'USER' ? 'user-message' : 'ai-message';
        messageElement.innerHTML = `
            <div class="message-content">${message.content}</div>
            <div class="message-time">${new Date(message.timestamp).toLocaleTimeString()}</div>
        `;
        messagesContainer.appendChild(messageElement);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

// 사용 예시
const chatSocket = new ChatWebSocket();

// 채팅 시작
async function startChat() {
    try {
        // 1. REST API로 채팅 세션 생성
        const response = await fetch('/api/v1/chat/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                memberId: 1,
                initialMessage: '배가 아프고 열이 나요'
            })
        });
        
        const result = await response.json();
        const sessionId = result.data.sessionId;
        
        // 2. WebSocket 연결
        chatSocket.connect(sessionId, 1);
        
    } catch (error) {
        console.error('채팅 시작 실패:', error);
    }
}
```

### 2.2 iOS (Swift) 연결

```swift
import StompClientLib

class ChatWebSocketManager {
    private var stompClient: StompClientLib?
    private var sessionId: Int?
    private var memberId: Int?
    
    func connect(sessionId: Int, memberId: Int) {
        self.sessionId = sessionId
        self.memberId = memberId
        
        let url = URL(string: "ws://13.124.250.98:8080/ws/chat")!
        stompClient = StompClientLib()
        stompClient?.openSocketWithURLRequest(request: NSURLRequest(url: url))
        
        // 연결 성공 콜백
        stompClient?.connect(headers: [:]) { [weak self] frame in
            print("WebSocket 연결 성공: \\(frame)")
            self?.subscribeToTopic()
        }
        
        // 에러 콜백
        stompClient?.errorCallBack = { error in
            print("WebSocket 에러: \\(error)")
        }
    }
    
    private func subscribeToTopic() {
        guard let sessionId = sessionId else { return }
        
        // 채팅방 구독
        stompClient?.subscribe(destination: "/topic/chat/\\(sessionId)") { [weak self] message in
            print("메시지 수신: \\(message.body)")
            
            if let data = message.body.data(using: .utf8),
               let chatMessage = try? JSONDecoder().decode(ChatMessage.self, from: data) {
                DispatchQueue.main.async {
                    self?.onMessageReceived(chatMessage)
                }
            }
        }
    }
    
    func sendMessage(content: String) {
        guard let sessionId = sessionId,
              let memberId = memberId else { return }
        
        let message = ChatMessageRequest(
            sessionId: sessionId,
            memberId: memberId,
            content: content,
            timestamp: ISO8601DateFormatter().string(from: Date())
        )
        
        if let messageData = try? JSONEncoder().encode(message),
           let messageString = String(data: messageData, encoding: .utf8) {
            
            stompClient?.sendMessage(message: messageString, toDestination: "/app/chat.send")
            print("메시지 전송: \\(content)")
        }
    }
    
    func disconnect() {
        stompClient?.disconnect()
        print("WebSocket 연결 종료")
    }
    
    private func onMessageReceived(_ message: ChatMessage) {
        // UI 업데이트 로직
        NotificationCenter.default.post(name: .newChatMessage, object: message)
    }
}

// 사용 예시
let chatManager = ChatWebSocketManager()

func startChatSession() {
    // 1. REST API로 채팅 세션 생성
    ChatAPI.startSession(memberId: 1, initialMessage: "배가 아프고 열이 나요") { result in
        switch result {
        case .success(let response):
            // 2. WebSocket 연결
            self.chatManager.connect(sessionId: response.sessionId, memberId: 1)
        case .failure(let error):
            print("채팅 시작 실패: \\(error)")
        }
    }
}
```

### 2.3 Android (Kotlin) 연결

```kotlin
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ChatWebSocketManager {
    private var stompClient: StompClient? = null
    private var sessionId: Long? = null
    private var memberId: Long? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    suspend fun connect(sessionId: Long, memberId: Long) {
        this.sessionId = sessionId
        this.memberId = memberId
        
        try {
            // WebSocket 클라이언트 생성
            val webSocketClient = OkHttpWebSocketClient()
            stompClient = StompClient(webSocketClient)
            
            // STOMP 세션 연결
            val session = stompClient!!.connect("ws://13.124.250.98:8080/ws/chat")
            Log.d(TAG, "WebSocket 연결 성공")
            
            // 채팅방 구독
            subscribeToTopic(session, sessionId)
            
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket 연결 실패", e)
        }
    }
    
    private suspend fun subscribeToTopic(session: StompSession, sessionId: Long) {
        coroutineScope.launch {
            try {
                // 메시지 구독
                session.subscribe("/topic/chat/$sessionId")
                    .collect { message ->
                        Log.d(TAG, "메시지 수신: ${message.bodyAsText}")
                        
                        val chatMessage = Json.decodeFromString<ChatMessage>(message.bodyAsText)
                        onMessageReceived(chatMessage)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "구독 실패", e)
            }
        }
    }
    
    suspend fun sendMessage(content: String) {
        sessionId?.let { sessionId ->
            memberId?.let { memberId ->
                try {
                    val message = ChatMessageRequest(
                        sessionId = sessionId,
                        memberId = memberId,
                        content = content,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val messageJson = Json.encodeToString(ChatMessageRequest.serializer(), message)
                    
                    stompClient?.let { client ->
                        val session = client.connect("ws://13.124.250.98:8080/ws/chat")
                        session.send("/app/chat.send", messageJson)
                        Log.d(TAG, "메시지 전송: $content")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "메시지 전송 실패", e)
                }
            }
        }
    }
    
    fun disconnect() {
        coroutineScope.cancel()
        stompClient = null
        Log.d(TAG, "WebSocket 연결 종료")
    }
    
    private fun onMessageReceived(message: ChatMessage) {
        // UI 업데이트
        _chatMessages.value = _chatMessages.value + message
    }
}

@Serializable
data class ChatMessageRequest(
    val sessionId: Long,
    val memberId: Long,
    val content: String,
    val timestamp: Long
)

@Serializable
data class ChatMessage(
    val id: Long,
    val sessionId: Long,
    val sender: String,
    val content: String,
    val timestamp: String,
    val messageType: String
)
```

---

## 📨 3. 메시지 송수신

### 3.1 메시지 전송 패턴

#### 사용자 메시지 → AI 응답
```javascript
// 1. 사용자가 메시지 입력
function sendUserMessage(content) {
    // UI에 사용자 메시지 표시
    displayMessage({
        sender: 'USER',
        content: content,
        timestamp: new Date().toISOString()
    });
    
    // WebSocket으로 메시지 전송
    chatSocket.sendMessage(content);
}

// 2. AI 응답 수신 및 표시
function onMessageReceived(message) {
    if (message.sender === 'AI') {
        // AI 응답 표시
        displayMessage(message);
        
        // 타이핑 효과 추가 (선택사항)
        typeWriterEffect(message.content);
        
        // 진료과 추천이 포함된 경우 별도 처리
        if (message.recommendedDepartment) {
            showDepartmentRecommendation(message.recommendedDepartment);
        }
    }
}
```

### 3.2 타이핑 효과 구현

```javascript
function typeWriterEffect(text, elementId) {
    const element = document.getElementById(elementId);
    element.innerHTML = '';
    
    let i = 0;
    const speed = 30; // 타이핑 속도 (ms)
    
    function typeWriter() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(typeWriter, speed);
        }
    }
    
    typeWriter();
}
```

### 3.3 진료과 추천 UI

```javascript
function showDepartmentRecommendation(recommendation) {
    const recommendationElement = document.createElement('div');
    recommendationElement.className = 'department-recommendation';
    recommendationElement.innerHTML = `
        <div class="recommendation-card">
            <h3>🏥 추천 진료과</h3>
            <div class="department-name">${recommendation.departmentName}</div>
            <div class="confidence">신뢰도: ${recommendation.confidence}%</div>
            <div class="reason">${recommendation.reason}</div>
            <button onclick="bookAppointment('${recommendation.departmentName}')">
                예약하기
            </button>
        </div>
    `;
    
    document.getElementById('chat-messages').appendChild(recommendationElement);
}
```

---

## 🎯 4. 실전 구현 예시

### 4.1 React 채팅 컴포넌트

```jsx
import React, { useState, useEffect, useRef } from 'react';
import { ChatWebSocket } from './ChatWebSocket';

const ChatComponent = ({ memberId }) => {
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [sessionId, setSessionId] = useState(null);
    const [isConnected, setIsConnected] = useState(false);
    const chatSocketRef = useRef(null);
    const messagesEndRef = useRef(null);
    
    // 채팅 시작
    const startChat = async (initialMessage) => {
        try {
            // REST API로 세션 생성
            const response = await fetch('/api/v1/chat/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    memberId: memberId,
                    initialMessage: initialMessage
                })
            });
            
            const result = await response.json();
            setSessionId(result.data.sessionId);
            setMessages(result.data.messages);
            
            // WebSocket 연결
            chatSocketRef.current = new ChatWebSocket();
            chatSocketRef.current.onConnected = () => setIsConnected(true);
            chatSocketRef.current.onMessageReceived = (message) => {
                setMessages(prev => [...prev, message]);
            };
            
            chatSocketRef.current.connect(result.data.sessionId, memberId);
            
        } catch (error) {
            console.error('채팅 시작 실패:', error);
        }
    };
    
    // 메시지 전송
    const sendMessage = () => {
        if (inputMessage.trim() && isConnected) {
            chatSocketRef.current.sendMessage(inputMessage);
            setInputMessage('');
        }
    };
    
    // 자동 스크롤
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);
    
    // 컴포넌트 언마운트 시 연결 해제
    useEffect(() => {
        return () => {
            if (chatSocketRef.current) {
                chatSocketRef.current.disconnect();
            }
        };
    }, []);
    
    return (
        <div className="chat-container">
            {!sessionId ? (
                <div className="chat-start">
                    <h2>AI 증상 상담</h2>
                    <input
                        type="text"
                        placeholder="현재 증상을 입력해주세요"
                        value={inputMessage}
                        onChange={(e) => setInputMessage(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && startChat(inputMessage)}
                    />
                    <button onClick={() => startChat(inputMessage)}>
                        상담 시작
                    </button>
                </div>
            ) : (
                <>
                    <div className="messages-container">
                        {messages.map((message, index) => (
                            <div
                                key={index}
                                className={`message ${message.sender === 'USER' ? 'user-message' : 'ai-message'}`}
                            >
                                <div className="message-content">
                                    {message.content}
                                </div>
                                <div className="message-time">
                                    {new Date(message.timestamp).toLocaleTimeString()}
                                </div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>
                    
                    <div className="input-container">
                        <input
                            type="text"
                            value={inputMessage}
                            onChange={(e) => setInputMessage(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                            placeholder="메시지를 입력하세요..."
                            disabled={!isConnected}
                        />
                        <button onClick={sendMessage} disabled={!isConnected}>
                            전송
                        </button>
                    </div>
                    
                    <div className="connection-status">
                        {isConnected ? '🟢 연결됨' : '🔴 연결 중...'}
                    </div>
                </>
            )}
        </div>
    );
};

export default ChatComponent;
```

### 4.2 CSS 스타일링

```css
.chat-container {
    display: flex;
    flex-direction: column;
    height: 500px;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
}

.messages-container {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    background-color: #f9f9f9;
}

.message {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
}

.user-message {
    align-items: flex-end;
}

.user-message .message-content {
    background-color: #007bff;
    color: white;
    padding: 8px 12px;
    border-radius: 18px 18px 4px 18px;
    max-width: 70%;
}

.ai-message .message-content {
    background-color: #e9ecef;
    color: #333;
    padding: 8px 12px;
    border-radius: 18px 18px 18px 4px;
    max-width: 70%;
}

.message-time {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
}

.input-container {
    display: flex;
    padding: 16px;
    border-top: 1px solid #ddd;
    background-color: white;
}

.input-container input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 20px;
    margin-right: 8px;
}

.input-container button {
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 20px;
    cursor: pointer;
}

.connection-status {
    padding: 8px 16px;
    background-color: #f8f9fa;
    font-size: 12px;
    text-align: center;
    border-top: 1px solid #ddd;
}
```

---

## 🔧 5. 디버깅 및 문제해결

### 5.1 연결 문제 해결

```javascript
class ChatWebSocketWithRetry extends ChatWebSocket {
    constructor() {
        super();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000; // 3초
    }
    
    connect(sessionId, memberId) {
        super.connect(sessionId, memberId);
        
        // 연결 실패 시 재시도 로직
        this.client.onWebSocketClose = (event) => {
            console.log('WebSocket 연결 끊어짐:', event);
            this.isConnected = false;
            
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
                setTimeout(() => {
                    console.log(`재연결 시도 ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts}`);
                    this.reconnectAttempts++;
                    this.connect(sessionId, memberId);
                }, this.reconnectInterval);
            } else {
                console.error('최대 재연결 횟수 초과');
                this.onMaxReconnectAttemptsReached();
            }
        };
    }
    
    onMaxReconnectAttemptsReached() {
        // UI에서 사용자에게 알림
        alert('채팅 연결이 불안정합니다. 페이지를 새로고침해주세요.');
    }
}
```

### 5.2 메시지 전송 실패 처리

```javascript
sendMessageWithRetry(content, maxRetries = 3) {
    let attempts = 0;
    
    const attemptSend = () => {
        if (this.isConnected) {
            this.sendMessage(content);
        } else {
            attempts++;
            if (attempts <= maxRetries) {
                setTimeout(() => {
                    console.log(`메시지 재전송 시도 ${attempts}/${maxRetries}`);
                    attemptSend();
                }, 1000);
            } else {
                console.error('메시지 전송 실패: 연결 불가');
                this.onMessageSendFailed(content);
            }
        }
    };
    
    attemptSend();
}

onMessageSendFailed(content) {
    // 로컬 스토리지에 임시 저장
    const failedMessages = JSON.parse(localStorage.getItem('failedMessages') || '[]');
    failedMessages.push({ content, timestamp: Date.now() });
    localStorage.setItem('failedMessages', JSON.stringify(failedMessages));
    
    // UI에 실패 알림
    this.showRetryOption(content);
}
```

### 5.3 로그 및 모니터링

```javascript
class ChatWebSocketLogger extends ChatWebSocket {
    constructor() {
        super();
        this.messageLog = [];
    }
    
    logMessage(type, message) {
        const logEntry = {
            type: type, // 'SENT', 'RECEIVED', 'ERROR'
            message: message,
            timestamp: new Date().toISOString(),
            sessionId: this.sessionId
        };
        
        this.messageLog.push(logEntry);
        
        // 서버로 로그 전송 (선택사항)
        this.sendLogToServer(logEntry);
    }
    
    sendMessage(content) {
        super.sendMessage(content);
        this.logMessage('SENT', { content });
    }
    
    onMessageReceived(message) {
        super.onMessageReceived(message);
        this.logMessage('RECEIVED', message);
    }
    
    // 로그 내보내기
    exportLogs() {
        const logData = JSON.stringify(this.messageLog, null, 2);
        const blob = new Blob([logData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = url;
        a.download = `chat-logs-${this.sessionId}-${Date.now()}.json`;
        a.click();
    }
}
```

---

## ⚠️ 주의사항

### 🔒 보안
- WebSocket 연결 시 적절한 인증 토큰 포함
- 메시지 내용에 개인정보 포함 시 암호화 고려
- CORS 설정으로 허용된 도메인에서만 접근

### 🎯 성능
- 메시지 기록은 적절한 수준으로 제한 (예: 최근 50개)
- 큰 파일 전송은 별도 API 사용
- 연결 재시도 간격 조절로 서버 부하 방지

### 📱 모바일 최적화
- 앱이 백그라운드로 갈 때 연결 일시 중단
- 네트워크 변경 시 자동 재연결
- 배터리 최적화 고려

---

## 📞 문의 및 지원

- **개발팀**: development@carefreepass.com  
- **WebSocket 기술 지원**: websocket@carefreepass.com
- **GitHub Issues**: https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_45_BE/issues

---

**최종 업데이트**: 2025년 9월 2일  
**버전**: 1.0.0  
**대상**: 프론트엔드 개발자 (웹/모바일)