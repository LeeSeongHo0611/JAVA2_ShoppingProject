let lastScrollTop = 0;
const footer = document.querySelector('.footer');

window.addEventListener('scroll', function() {
    const currentScroll = window.scrollY;

    if (currentScroll > lastScrollTop) {
        // 아래로 스크롤 시
        footer.style.transform = 'translateY(100%)'; // footer 숨기기
    } else {
        // 위로 스크롤 시
        footer.style.transform = 'translateY(0)'; // footer 보이기
    }

    // 스크롤 위치가 문서의 끝에 도달했을 때
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight) {
        footer.style.transform = 'translateY(0)'; // footer 보이기
    }

    lastScrollTop = currentScroll; // 현재 스크롤 위치 업데이트
});