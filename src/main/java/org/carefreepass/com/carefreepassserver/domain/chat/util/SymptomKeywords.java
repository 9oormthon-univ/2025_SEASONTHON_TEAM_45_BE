package org.carefreepass.com.carefreepassserver.domain.chat.util;

import java.util.List;

public final class SymptomKeywords {
    
    public static final List<String> DENTAL_KEYWORDS = List.of(
        "치아", "치통", "잇몸", "충치", "이빨", "사랑니", "구강"
    );
    
    public static final List<String> ORTHOPEDIC_KEYWORDS = List.of(
        "골절", "삐었", "발목", "무릎", "어깨", "허리", "관절", "뼈", "근육통", "염좌"
    );
    
    public static final List<String> ENT_KEYWORDS = List.of(
        "목소리", "목구멍", "인후", "콧물", "코막힘", "귀", "중이염", "편도", "성대"
    );
    
    public static final List<String> DERMATOLOGY_KEYWORDS = List.of(
        "여드름", "알레르기", "발진", "가려움", "습진", "두드러기", "점", "사마귀", "피부"
    );
    
    public static final List<String> SURGERY_KEYWORDS = List.of(
        "수술", "상처", "봉합", "찢어", "베", "외상", "화상", "절개"
    );
    
    public static final List<String> INTERNAL_MEDICINE_KEYWORDS = List.of(
        "소화불량", "설사", "변비", "위", "장", "간", "당뇨", "고혈압", "감기", "몸살"
    );
    
    public static final List<String> VAGUE_SYMPTOMS = List.of(
        "아파", "아픈", "불편", "이상", "문제", "좀", "뭔가", "아프", "힘들", "괜찮"
    );
    
    public static final List<String> SPECIFIC_BODY_PARTS = List.of(
        "머리", "두통", "목", "어깨", "가슴", "배", "등", "허리", "팔", "다리", "발", "손",
        "치아", "잇몸", "피부", "관절", "뼈"
    );
    
    public static final List<String> HEAD_PAIN_KEYWORDS = List.of(
        "머리", "두통", "어지러"
    );
    
    public static final List<String> THROAT_EAR_NOSE_KEYWORDS = List.of(
        "목구멍", "귀", "코", "인후"
    );
    
    public static final List<String> CHEST_HEART_KEYWORDS = List.of(
        "가슴", "심장", "호흡", "숨"
    );
    
    public static final List<String> STOMACH_KEYWORDS = List.of(
        "배", "소화", "위", "장"
    );
    
    public static final List<String> LIMBS_JOINT_KEYWORDS = List.of(
        "팔", "다리", "관절", "뼈", "근육"
    );
    
    public static final List<String> SKIN_KEYWORDS = List.of(
        "피부", "발진", "가려"
    );
    
    public static final List<String> ORAL_KEYWORDS = List.of(
        "치아", "잇몸", "구강"
    );
    
    public static final List<String> TRAUMA_KEYWORDS = List.of(
        "상처", "베", "화상", "외상"
    );
    
    private SymptomKeywords() {
        // 유틸리티 클래스는 인스턴스화하지 않음
    }
}