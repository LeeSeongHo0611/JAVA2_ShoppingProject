document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('kakaopay').addEventListener('click', kakaopay);
});


function kakaopay() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/order";
    var paramData = {
        itemId: $("#itemId").val(),
        count: $("#count").val()
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: param,
        beforeSend: function(xhr) {
            /*데이터 전송하기 전에 헤더이 csrf 값을 설정*/
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        cache: false,
        success: function(result, status) {
            if (result.orderId) {
                // 주문이 성공적으로 완료되었으면 카카오 페이 결제를 진행합니다.
                processKakaoPayment(result, token, header);
            } else {
                alert("주문 처리 중 오류가 발생했습니다.");
            }
        },
        error: function(jqXHR, status, error) {
            if (jqXHR.status == '401') {
                alert('로그인 후 이용해주세요.');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}

function processKakaoPayment(orderData, token, header) {
    const IMP = window.IMP; // 생략 가능
    IMP.init('imp12504213'); // 포트원의 고객사 식별코드

    IMP.request_pay({
        pg: 'kakaopay', // 'kakao'는 PG사, 'TC0ONETIME'은 테스트용 카카오페이 상점아이디(CID)
        pay_method: 'kakaopay', // 결제수단
        merchant_uid: 'merchant_' + new Date().getTime(), // 주문번호
        name: orderData.itemName, // 주문명
        amount: orderData.amount, // 금액
        buyer_email: orderData.buyerEmail,
        buyer_name: orderData.buyerName,
        buyer_tel: orderData.buyerTel,
        buyer_addr: orderData.buyerAddr,
        buyer_postcode: orderData.buyerPostcode
    }, function(rsp) { // callback
        if (rsp.success) {
            alert('결제가 완료되었습니다.');
            // 결제 성공 시 서버에 결제 정보를 전송하여 저장하는 로직 추가
            savePaymentInfo(rsp, orderData.orderId, token, header);
        } else {
            alert('결제에 실패하였습니다. 에러 내용: ' + rsp.error_msg);
        }
    });
}

function savePaymentInfo(paymentInfo, orderId, token, header) {
    $.ajax({
        url: '/payment/complete',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ paymentInfo, orderId }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            alert("결제 정보가 저장되었습니다.");
            location.href = '/orders'; // 결제 후 주문 페이지로 리다이렉트
        },
        error: function(jqXHR, status, error) {
            alert('결제 정보 저장 실패: ' + jqXHR.responseText);
        }
    });
}
