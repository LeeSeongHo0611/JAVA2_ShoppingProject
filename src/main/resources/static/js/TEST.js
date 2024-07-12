$(document).ready(function(){
    $("input[name=cartChkBox]").change(function(){
        getOrderTotalPrice();
    });
});

function getOrderTotalPrice(){
    var orderTotalPrice = 0;
    $("input[name=cartChkBox]:checked").each(function(){
        var cartItemId = $(this).val();
        var price = $("#price_" + cartItemId).attr("data-price");
        var count = $("#count_" + cartItemId).val();
        orderTotalPrice += price * count;
    });

    $("#orderTotalPrice").html(orderTotalPrice+'원');
}

function changeCount(obj){
    var count = obj.value;
    var cartItemId = obj.id.split('_')[1];
    var price = $("#price_"+cartItemId).data("price");
    var totalPrice = count * price;
    $("#totalPrice_" + cartItemId).html(totalPrice+"원");
    getOrderTotalPrice();
    updateCartItemCount(cartItemId, count);
}
function checkAll(){
    if($("#checkall").prop("checked")){
        $("input[name=cartChkBox]").prop("checked",true);
    }
    else{
        $("input[name=cartChkBox]").prop("checked",false);
    }
    getOrderTotalPrice();
}

function updateCartItemCount(cartItemId, count){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cartItem/" + cartItemId + "?count=" + count;

    $.ajax({
        url : url,
        type : "PATCH",
        beforeSend : function(xhr){
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache : false,
        success : function(result, status){
            console.log("cartItem count update success");
        },
        error : function(jqXHR, status, error){
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요.');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });
}

function deleteCartItem(obj){
    var cartItemId = obj.dataset.id;
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cartItem/" + cartItemId;

    $.ajax({
        url : url,
        type : "DELETE",
        beforeSend : function(xhr){
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache : false,
        success : function(result, status){
            location.href='/cart'; // 다시 본인을 부름 이유 삭제로 인한 화면 변경이 필요하기 때문
        },
        error : function(jqXHR, status, error){
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요.');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });
}

function orders(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cart/orders";

    var dataList = new Array(); // 배열 생성
    var paramData = new Object(); // 객체 생성

    //체크된 카트 확인을 위한 함수
    $("input[name=cartChkBox]:checked").each(function(){
        var cartItemId = $(this).val(); // 체크된 객체의 값을 cartItemId에 대입
        var data = new Object(); // 객체 생성
        data["cartItemId"] = cartItemId; // 객체의 키("cartItemId")에 위에 선언된 cartItemId 변수(value) 대입
        dataList.push(data); // data 객체를 dataList 배열에 push 함
    });

    paramData['cartOrderDtoList'] = dataList;
     var param = JSON.stringify(paramData);

    $.ajax({
        url : url,
        type : "POST",
        contentType : "application/json",
        data : param,
        beforeSend : function(xhr){
            /*데이터 전송하기 전에 헤더이 csrf 값을 설정*/
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache : false,
        success : function(result, status){
            alert("주문이 완료 되었습니다.");
            location.href='/orders';
        },
        error : function(jqXHR, status, error){
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요.');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });
}