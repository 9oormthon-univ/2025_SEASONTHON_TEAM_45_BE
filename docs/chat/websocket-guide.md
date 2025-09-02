# WebSocket 연결 가이드

## 📋 개요

CareFreePass 시스템의 WebSocket을 통한 실시간 채팅 기능 사용 가이드입니다.

## 🔧 서버 설정

### WebSocket 설정 (WebSocketConfig.java)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지를 전송하는 브로커 활성화
        config.enableSimpleBroker("/topic", "/queue");
        // 클라이언트에서 서버로 메시지 전송 시 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")  // CORS 설정
                .withSockJS();  // SockJS 사용
    }
}
```

### 채팅 메시지 핸들러 (ChatController.java)

```java
@MessageMapping("/chat.send")
@SendTo("/topic/chat")
public ChatMessageResponse sendMessageViaWebSocket(ChatMessageRequest request) {
    // WebSocket을 통한 실시간 메시징 처리
    ChatMessage aiMessage = aiChatService.sendMessage(
        request.getSessionId(),
        request.getMemberId(),
        request.getContent()
    );
    return ChatMessageResponse.from(aiMessage);
}
```

## 🌐 클라이언트 연결 방법

### 1. JavaScript (SockJS + STOMP)

#### 필요한 라이브러리
```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
```

#### 연결 설정
```javascript
// WebSocket 연결 설정
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = new StompJs.Client({
    webSocketFactory: () => socket,
    debug: (str) => {
        console.log('STOMP Debug:', str);
    },
    onConnect: (frame) => {
        console.log('WebSocket 연결 성공:', frame);
        
        // 메시지 구독
        stompClient.subscribe('/topic/chat', (message) => {
            const chatMessage = JSON.parse(message.body);
            displayMessage(chatMessage);
        });
    },
    onStompError: (frame) => {
        console.error('STOMP 오류:', frame.headers['message']);
        console.error('Details:', frame.body);
    }
});

// 연결 시작
stompClient.activate();
```

#### 메시지 전송
```javascript
function sendMessage() {
    const messageData = {
        sessionId: 1,
        memberId: 1,
        content: document.getElementById('messageInput').value
    };
    
    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: '/app/chat.send',
            body: JSON.stringify(messageData)
        });
    }
}
```

#### 메시지 수신 처리
```javascript
function displayMessage(chatMessage) {
    const chatArea = document.getElementById('chatArea');
    const messageElement = document.createElement('div');
    
    messageElement.innerHTML = `
        <div class="message ${chatMessage.sender === 'USER' ? 'user' : 'ai'}">
            <strong>${chatMessage.sender}:</strong> ${chatMessage.content}
            <div class="timestamp">${new Date(chatMessage.timestamp).toLocaleString()}</div>
        </div>
    `;
    
    chatArea.appendChild(messageElement);
    chatArea.scrollTop = chatArea.scrollHeight;
}
```

### 2. React/Vue.js 예시

#### React 컴포넌트
```jsx
import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const ChatComponent = () => {
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const stompClient = useRef(null);

    useEffect(() => {
        // WebSocket 연결
        const socket = new SockJS('http://localhost:8080/ws/chat');
        stompClient.current = Stomp.over(socket);
        
        stompClient.current.connect({}, (frame) => {
            console.log('Connected:', frame);
            
            // 메시지 구독
            stompClient.current.subscribe('/topic/chat', (message) => {
                const newMessage = JSON.parse(message.body);
                setMessages(prev => [...prev, newMessage]);
            });
        });

        return () => {
            if (stompClient.current) {
                stompClient.current.disconnect();
            }
        };
    }, []);

    const sendMessage = () => {
        if (stompClient.current && inputMessage.trim()) {
            const messageData = {
                sessionId: 1,
                memberId: 1,
                content: inputMessage
            };
            
            stompClient.current.send('/app/chat.send', {}, JSON.stringify(messageData));
            setInputMessage('');
        }
    };

    return (
        <div className="chat-container">
            <div className="messages">
                {messages.map((msg, index) => (
                    <div key={index} className={`message ${msg.sender.toLowerCase()}`}>
                        <strong>{msg.sender}:</strong> {msg.content}
                    </div>
                ))}
            </div>
            <div className="input-area">
                <input
                    type="text"
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                />
                <button onClick={sendMessage}>전송</button>
            </div>
        </div>
    );
};
```

### 3. Flutter (Dart) 예시

#### 의존성 추가
```yaml
dependencies:
  stomp_dart_client: ^2.0.0
```

#### Dart 코드
```dart
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

class ChatService {
  StompClient? stompClient;
  
  void connect() {
    stompClient = StompClient(
      config: StompConfig(
        url: 'http://localhost:8080/ws/chat',
        onConnect: onConnect,
        beforeConnect: () async {
          print('WebSocket 연결 시도 중...');
        },
        onWebSocketError: (dynamic error) => print('WebSocket 오류: $error'),
      ),
    );
    
    stompClient!.activate();
  }
  
  void onConnect(StompFrame frame) {
    print('WebSocket 연결 성공');
    
    // 메시지 구독
    stompClient!.subscribe(
      destination: '/topic/chat',
      callback: (frame) {
        if (frame.body != null) {
          final message = jsonDecode(frame.body!);
          // 메시지 처리 로직
          handleIncomingMessage(message);
        }
      },
    );
  }
  
  void sendMessage(String content) {
    if (stompClient != null) {
      final messageData = {
        'sessionId': 1,
        'memberId': 1,
        'content': content,
      };
      
      stompClient!.send(
        destination: '/app/chat.send',
        body: jsonEncode(messageData),
      );
    }
  }
  
  void handleIncomingMessage(Map<String, dynamic> message) {
    // UI 업데이트 로직
    print('새 메시지: ${message['content']}');
  }
  
  void disconnect() {
    stompClient?.deactivate();
  }
}
```

## 📡 메시지 프로토콜

### 클라이언트 → 서버 (메시지 전송)

**Destination:** `/app/chat.send`

**Payload:**
```json
{
  "sessionId": 1,
  "memberId": 1,
  "content": "안녕하세요, 복통이 있어요"
}
```

### 서버 → 클라이언트 (메시지 수신)

**Topic:** `/topic/chat`

**Response:**
```json
{
  "id": 123,
  "sessionId": 1,
  "sender": "AI",
  "content": "안녕하세요! 복통 증상에 대해 자세히 알려주세요.",
  "timestamp": "2025-09-02T11:30:00",
  "messageType": "TEXT"
}
```

## 🔍 연결 테스트 방법

### 1. 브라우저 콘솔 테스트

```javascript
// 콘솔에서 직접 실행
const socket = new SockJS('http://localhost:8080/ws/chat');
const client = new StompJs.Client({
    webSocketFactory: () => socket,
    onConnect: () => {
        console.log('연결됨!');
        client.subscribe('/topic/chat', (msg) => {
            console.log('받은 메시지:', JSON.parse(msg.body));
        });
        
        // 테스트 메시지 전송
        client.publish({
            destination: '/app/chat.send',
            body: JSON.stringify({
                sessionId: 1,
                memberId: 1,
                content: '테스트 메시지'
            })
        });
    }
});
client.activate();
```

### 2. Postman WebSocket 테스트

1. **Postman 새 요청 생성**
2. **프로토콜을** WebSocket으로 선택
3. **URL:** `ws://localhost:8080/ws/chat`
4. **연결 후 STOMP 명령 전송:**
   ```
   CONNECT
   accept-version:1.0,1.1,2.0
   heart-beat:10000,10000
   
   
   ```

## 🛠️ 문제 해결

### 일반적인 오류들

#### 1. 연결 실패 (ERR_CONNECTION_REFUSED)
```bash
# 서버가 실행 중인지 확인
curl http://localhost:8080/actuator/health

# WebSocket 엔드포인트 확인
curl -I http://localhost:8080/ws/chat
```

#### 2. CORS 오류
- `WebSocketConfig.java`에서 `setAllowedOriginPatterns("*")` 확인
- 프론트엔드에서 정확한 서버 URL 사용

#### 3. STOMP 연결 오류
```javascript
// 디버그 모드 활성화
const stompClient = new StompJs.Client({
    debug: (str) => console.log(str), // 모든 STOMP 메시지 로깅
    // ... 기타 설정
});
```

#### 4. 메시지 전송 안됨
```javascript
// 연결 상태 확인
if (stompClient.connected) {
    // 메시지 전송
    stompClient.publish({...});
} else {
    console.error('STOMP 클라이언트가 연결되지 않았습니다');
}
```

## 🔐 보안 고려사항

### 1. CORS 설정 (프로덕션)
```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws/chat")
            .setAllowedOrigins("https://yourdomain.com") // 특정 도메인만 허용
            .withSockJS();
}
```

### 2. 인증 처리
```java
@MessageMapping("/chat.send")
@SendTo("/topic/chat")
public ChatMessageResponse sendMessageViaWebSocket(
        @Header("Authorization") String authToken,
        ChatMessageRequest request) {
    // JWT 토큰 검증
    if (!jwtUtil.validateToken(authToken)) {
        throw new SecurityException("유효하지 않은 토큰");
    }
    // ...
}
```

## 📱 모바일 앱 연동

### React Native
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const connectWebSocket = () => {
    const socket = new SockJS('http://your-server-ip:8080/ws/chat');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, (frame) => {
        console.log('연결됨:', frame);
        // 구독 및 메시지 처리
    });
};
```

### iOS (Swift)
```swift
import Starscream

class WebSocketManager {
    var socket: WebSocket?
    
    func connect() {
        var request = URLRequest(url: URL(string: "ws://localhost:8080/ws/chat")!)
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }
}

extension WebSocketManager: WebSocketDelegate {
    func didReceive(event: WebSocketEvent, client: WebSocket) {
        switch event {
        case .connected(let headers):
            print("websocket is connected: \\(headers)")
        case .text(let string):
            print("Received text: \\(string)")
        // 기타 이벤트 처리
        }
    }
}
```

## 🎯 사용 예시

### 실시간 채팅 시나리오

1. **환자가 증상 입력**
   ```javascript
   stompClient.publish({
       destination: '/app/chat.send',
       body: JSON.stringify({
           sessionId: 1,
           memberId: 1,
           content: '머리가 아프고 열이 나요'
       })
   });
   ```

2. **AI가 응답 전송**
   ```json
   {
       "sender": "AI",
       "content": "증상을 자세히 알려주세요. 언제부터 아프기 시작했나요?",
       "timestamp": "2025-09-02T11:30:00"
   }
   ```

3. **모든 연결된 클라이언트가 실시간으로 메시지 수신**

---

## 📞 문의 및 지원

- **개발팀 연락처**: development@carefreepass.com
- **기술 지원**: support@carefreepass.com

**최종 업데이트**: 2025년 9월 2일
**작성자**: CareFreePass 개발팀