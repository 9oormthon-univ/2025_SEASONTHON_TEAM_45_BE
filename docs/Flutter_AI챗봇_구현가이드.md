# 🤖 Flutter AI 챗봇 구현 가이드

## 📋 개요

Flutter 앱에서 CareFreePass AI 챗봇을 구현하여 환자의 증상 상담 및 예약 기능을 제공하는 방법을 설명합니다.

## 🎯 주요 기능

1. **실시간 채팅 인터페이스**
2. **증상 입력 및 AI 분석**  
3. **진료과 추천 받기**
4. **AI 추천 후 바로 예약하기**
5. **대화 내역 저장 및 조회**

## 🔧 Flutter 프로젝트 설정

### 1단계: 의존성 추가

#### pubspec.yaml
```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # HTTP 통신용
  http: ^1.1.0
  dio: ^5.3.2
  
  # 상태 관리용  
  provider: ^6.1.1
  riverpod: ^2.4.9
  
  # UI 컴포넌트
  flutter_chat_ui: ^1.6.10
  flutter_chat_types: ^3.6.2
  
  # 로컬 저장용
  shared_preferences: ^2.2.2
  sqflite: ^2.3.0
  
  # 웹소켓 (선택사항)
  web_socket_channel: ^2.4.0
  socket_io_client: ^2.0.3
```

### 2단계: API 서비스 클래스 생성

#### lib/services/api_service.dart
```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiService {
  static const String baseUrl = 'http://localhost:8080/api/v1';
  
  // 채팅 시작
  Future<Map<String, dynamic>> startChat(int memberId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/start'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'memberId': memberId}),
    );
    
    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception('채팅 시작 실패');
    }
  }
  
  // 메시지 전송
  Future<Map<String, dynamic>> sendMessage(
    int sessionId, 
    String message
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/message'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({
        'sessionId': sessionId,
        'message': message,
        'timestamp': DateTime.now().toIso8601String()
      }),
    );
    
    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception('메시지 전송 실패');
    }
  }
  
  // 대화 내역 조회
  Future<List<dynamic>> getChatHistory(int memberId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/chat/history?memberId=$memberId'),
      headers: {'Content-Type': 'application/json'},
    );
    
    if (response.statusCode == 200) {
      return json.decode(response.body)['body'];
    } else {
      throw Exception('대화 내역 조회 실패');
    }
  }
  
  // AI 추천 후 예약하기
  Future<Map<String, dynamic>> bookAppointmentFromAI({
    required int memberId,
    required String department,
    required String appointmentDate,
    required String appointmentTime,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/book'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({
        'memberId': memberId,
        'hospitalName': '서울대병원',
        'department': department,
        'appointmentDate': appointmentDate,
        'appointmentTime': appointmentTime,
      }),
    );
    
    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception('예약 실패');
    }
  }
}
```

## 💬 채팅 UI 구현

### 3단계: 채팅 모델 클래스

#### lib/models/chat_message.dart
```dart
class ChatMessage {
  final String id;
  final String text;
  final DateTime timestamp;
  final bool isUser;
  final String? department;
  final String? severity;
  final bool? needsImmediateAttention;
  
  ChatMessage({
    required this.id,
    required this.text,
    required this.timestamp,
    required this.isUser,
    this.department,
    this.severity,
    this.needsImmediateAttention,
  });
  
  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    return ChatMessage(
      id: json['id'] ?? DateTime.now().millisecondsSinceEpoch.toString(),
      text: json['message'] ?? json['text'],
      timestamp: DateTime.parse(json['timestamp']),
      isUser: json['isUser'] ?? false,
      department: json['recommendedDepartment'],
      severity: json['severity'],
      needsImmediateAttention: json['needsImmediateAttention'],
    );
  }
}
```

### 4단계: 채팅 상태 관리

#### lib/providers/chat_provider.dart
```dart
import 'package:flutter/material.dart';
import '../models/chat_message.dart';
import '../services/api_service.dart';

class ChatProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  List<ChatMessage> _messages = [];
  bool _isLoading = false;
  int? _sessionId;
  int? _memberId;
  
  List<ChatMessage> get messages => _messages;
  bool get isLoading => _isLoading;
  
  // 채팅 시작
  Future<void> startChat(int memberId) async {
    try {
      _isLoading = true;
      _memberId = memberId;
      notifyListeners();
      
      final response = await _apiService.startChat(memberId);
      _sessionId = response['body']['sessionId'];
      
      // 환영 메시지 추가
      _addMessage(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        text: "안녕하세요! 저는 CareFreePass AI 상담사입니다. 어떤 증상으로 문의드리나요?",
        timestamp: DateTime.now(),
        isUser: false,
      ));
      
    } catch (e) {
      print('채팅 시작 오류: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
  
  // 메시지 전송
  Future<void> sendMessage(String text) async {
    if (_sessionId == null) return;
    
    // 사용자 메시지 추가
    _addMessage(ChatMessage(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      text: text,
      timestamp: DateTime.now(),
      isUser: true,
    ));
    
    try {
      _isLoading = true;
      notifyListeners();
      
      final response = await _apiService.sendMessage(_sessionId!, text);
      final aiResponse = response['body'];
      
      // AI 응답 추가
      _addMessage(ChatMessage.fromJson({
        'message': aiResponse['message'],
        'timestamp': DateTime.now().toIso8601String(),
        'isUser': false,
        'recommendedDepartment': aiResponse['recommendedDepartment'],
        'severity': aiResponse['severity'],
        'needsImmediateAttention': aiResponse['needsImmediateAttention'],
      }));
      
    } catch (e) {
      _addMessage(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        text: "죄송합니다. 잠시 후 다시 시도해주세요.",
        timestamp: DateTime.now(),
        isUser: false,
      ));
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
  
  void _addMessage(ChatMessage message) {
    _messages.insert(0, message);
    notifyListeners();
  }
  
  // 예약하기
  Future<bool> bookAppointment(String department, String date, String time) async {
    if (_memberId == null) return false;
    
    try {
      await _apiService.bookAppointmentFromAI(
        memberId: _memberId!,
        department: department,
        appointmentDate: date,
        appointmentTime: time,
      );
      
      _addMessage(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        text: "✅ 예약이 완료되었습니다!\n📅 $date $time\n🏥 $department",
        timestamp: DateTime.now(),
        isUser: false,
      ));
      
      return true;
    } catch (e) {
      _addMessage(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        text: "❌ 예약 중 오류가 발생했습니다. 다시 시도해주세요.",
        timestamp: DateTime.now(),
        isUser: false,
      ));
      return false;
    }
  }
}
```

### 5단계: 채팅 화면 UI

#### lib/screens/chat_screen.dart
```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/chat_provider.dart';
import '../models/chat_message.dart';

class ChatScreen extends StatefulWidget {
  final int memberId;
  
  const ChatScreen({Key? key, required this.memberId}) : super(key: key);
  
  @override
  _ChatScreenState createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<ChatProvider>(context, listen: false)
          .startChat(widget.memberId);
    });
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('🤖 AI 상담'),
        backgroundColor: Colors.blue[600],
        elevation: 0,
      ),
      body: Column(
        children: [
          // 메시지 목록
          Expanded(
            child: Consumer<ChatProvider>(
              builder: (context, chatProvider, child) {
                return ListView.builder(
                  controller: _scrollController,
                  reverse: true,
                  padding: EdgeInsets.all(16),
                  itemCount: chatProvider.messages.length,
                  itemBuilder: (context, index) {
                    final message = chatProvider.messages[index];
                    return _buildMessageBubble(message, context);
                  },
                );
              },
            ),
          ),
          
          // 로딩 인디케이터
          Consumer<ChatProvider>(
            builder: (context, chatProvider, child) {
              return chatProvider.isLoading
                  ? Container(
                      padding: EdgeInsets.all(8),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          ),
                          SizedBox(width: 8),
                          Text('AI가 분석 중입니다...')
                        ],
                      ),
                    )
                  : SizedBox.shrink();
            },
          ),
          
          // 입력창
          _buildInputArea(),
        ],
      ),
    );
  }
  
  Widget _buildMessageBubble(ChatMessage message, BuildContext context) {
    final isUser = message.isUser;
    
    return Container(
      margin: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: [
          if (!isUser)
            CircleAvatar(
              backgroundColor: Colors.blue[100],
              child: Icon(Icons.smart_toy, color: Colors.blue[600]),
              radius: 16,
            ),
          
          SizedBox(width: 8),
          
          Flexible(
            child: Container(
              padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: isUser ? Colors.blue[600] : Colors.grey[200],
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    message.text,
                    style: TextStyle(
                      color: isUser ? Colors.white : Colors.black87,
                      fontSize: 16,
                    ),
                  ),
                  
                  // AI 추천 정보 표시
                  if (!isUser && message.department != null)
                    _buildRecommendationCard(message, context),
                ],
              ),
            ),
          ),
          
          SizedBox(width: 8),
          
          if (isUser)
            CircleAvatar(
              backgroundColor: Colors.grey[300],
              child: Icon(Icons.person, color: Colors.grey[600]),
              radius: 16,
            ),
        ],
      ),
    );
  }
  
  Widget _buildRecommendationCard(ChatMessage message, BuildContext context) {
    return Container(
      margin: EdgeInsets.only(top: 8),
      padding: EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.blue[200]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '💡 추천 진료과',
            style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue[800]),
          ),
          SizedBox(height: 4),
          Text('🏥 ${message.department}'),
          if (message.severity != null)
            Text('⚠️ 심각도: ${message.severity}'),
          
          SizedBox(height: 8),
          
          ElevatedButton.icon(
            onPressed: () => _showBookingDialog(message.department!),
            icon: Icon(Icons.calendar_today, size: 16),
            label: Text('바로 예약하기'),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.blue[600],
              foregroundColor: Colors.white,
            ),
          ),
        ],
      ),
    );
  }
  
  Widget _buildInputArea() {
    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            offset: Offset(0, -2),
            blurRadius: 4,
            color: Colors.black.withOpacity(0.1),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _textController,
              decoration: InputDecoration(
                hintText: '증상을 입력해주세요...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20),
                ),
                contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              ),
              onSubmitted: _sendMessage,
            ),
          ),
          SizedBox(width: 8),
          IconButton(
            onPressed: () => _sendMessage(_textController.text),
            icon: Icon(Icons.send),
            color: Colors.blue[600],
            iconSize: 28,
          ),
        ],
      ),
    );
  }
  
  void _sendMessage(String text) {
    if (text.trim().isEmpty) return;
    
    Provider.of<ChatProvider>(context, listen: false).sendMessage(text.trim());
    _textController.clear();
    
    // 스크롤을 맨 아래로
    Future.delayed(Duration(milliseconds: 100), () {
      _scrollController.animateTo(
        0,
        duration: Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    });
  }
  
  void _showBookingDialog(String department) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('📅 예약하기'),
          content: Text('$department 진료과로 예약하시겠습니까?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: Text('취소'),
            ),
            ElevatedButton(
              onPressed: () {
                Navigator.of(context).pop();
                // 예약 화면으로 이동하거나 직접 예약
                _proceedWithBooking(department);
              },
              child: Text('예약하기'),
            ),
          ],
        );
      },
    );
  }
  
  void _proceedWithBooking(String department) {
    // 실제 예약 로직 - 날짜/시간 선택 후 예약
    final chatProvider = Provider.of<ChatProvider>(context, listen: false);
    
    // 예시: 내일 오전 10시로 예약
    final tomorrow = DateTime.now().add(Duration(days: 1));
    final dateString = "${tomorrow.year}-${tomorrow.month.toString().padLeft(2, '0')}-${tomorrow.day.toString().padLeft(2, '0')}";
    
    chatProvider.bookAppointment(department, dateString, "10:00");
  }
}
```

## 🌐 웹소켓 연결 (선택사항)

실시간 통신이 필요한 경우 웹소켓을 사용할 수 있습니다.

### 웹소켓 서비스 구현

#### lib/services/websocket_service.dart
```dart
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebSocketService {
  WebSocketChannel? _channel;
  Function(Map<String, dynamic>)? onMessage;
  
  void connect(int memberId) {
    try {
      _channel = WebSocketChannel.connect(
        Uri.parse('ws://localhost:8080/ws/chat/$memberId')
      );
      
      _channel!.stream.listen(
        (message) {
          final data = json.decode(message);
          onMessage?.call(data);
        },
        onError: (error) {
          print('웹소켓 오류: $error');
        },
        onDone: () {
          print('웹소켓 연결 종료');
        },
      );
    } catch (e) {
      print('웹소켓 연결 실패: $e');
    }
  }
  
  void sendMessage(Map<String, dynamic> message) {
    if (_channel != null) {
      _channel!.sink.add(json.encode(message));
    }
  }
  
  void disconnect() {
    _channel?.sink.close();
    _channel = null;
  }
}
```

### ChatProvider에 웹소켓 통합

#### lib/providers/chat_provider.dart (웹소켓 버전)
```dart
class ChatProvider with ChangeNotifier {
  final WebSocketService _wsService = WebSocketService();
  
  @override
  void dispose() {
    _wsService.disconnect();
    super.dispose();
  }
  
  Future<void> startChat(int memberId) async {
    // 웹소켓 연결
    _wsService.onMessage = (data) {
      final message = ChatMessage.fromJson(data);
      _addMessage(message);
    };
    
    _wsService.connect(memberId);
    
    // 기존 HTTP API 로직...
  }
  
  Future<void> sendMessage(String text) async {
    // 웹소켓으로 메시지 전송
    _wsService.sendMessage({
      'type': 'chat_message',
      'message': text,
      'timestamp': DateTime.now().toIso8601String()
    });
    
    // 사용자 메시지 즉시 표시
    _addMessage(ChatMessage(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      text: text,
      timestamp: DateTime.now(),
      isUser: true,
    ));
  }
}
```

## 🚀 실행 방법

### 1. 의존성 설치
```bash
flutter pub get
```

### 2. 앱 실행
```bash
flutter run
```

### 3. 메인 앱에서 사용
```dart
// main.dart
import 'package:provider/provider.dart';
import 'providers/chat_provider.dart';
import 'screens/chat_screen.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (context) => ChatProvider(),
      child: MyApp(),
    ),
  );
}

// 채팅 화면 열기
Navigator.push(
  context,
  MaterialPageRoute(
    builder: (context) => ChatScreen(memberId: currentMemberId),
  ),
);
```

## 📱 추가 기능 구현

### 1. 음성 입력
```yaml
dependencies:
  speech_to_text: ^6.6.0
```

### 2. 이미지 전송
```yaml
dependencies:
  image_picker: ^1.0.4
```

### 3. 푸시 알림 (FCM)
```yaml
dependencies:
  firebase_messaging: ^14.7.9
```

## 🔐 보안 고려사항

1. **API 키 보안**: 환경변수나 secure storage 사용
2. **HTTPS 통신**: 프로덕션에서는 반드시 HTTPS 사용
3. **토큰 관리**: JWT 토큰 자동 갱신 구현
4. **개인정보 보호**: 로컬 저장 시 암호화

## 📊 성능 최적화

1. **메시지 페이지네이션**: 대화 내역이 많을 때 부분 로딩
2. **이미지 캐싱**: 프로필 이미지나 첨부 이미지 캐싱
3. **상태 관리 최적화**: Riverpod 사용 시 불필요한 리빌드 방지

---

**완료 시간**: 약 2-3일  
**난이도**: 중급-고급  
**필요 기술**: Flutter, HTTP, WebSocket, 상태관리