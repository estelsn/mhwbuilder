//사용자가 실제로 보게 되는 화면의 가장 큰 단위. 메인 페이지의 구조를 작성하며, 다른 작은 컴포넌트들을 불러와서 조립하는 장소. 현재 진행 중인 프로젝트의 핵심 로직이나 라우팅 설정이 시작되는 곳

import Header from "./components/Header";
import LeftPanel from "./components/LeftPanel";
import ResultPanel from "./components/ResultPanel";
import DetailPanel from "./components/DetailPanel";

function App() {
  return (
    <div className="min-h-screen bg-[#131313] text-[#e5e2e1]">
      <Header />
       <main className="h-[calc(100vh-72px)] pt-[72px] flex overflow-hidden">
        <LeftPanel />
        <ResultPanel />
        <DetailPanel />
      </main>
    </div>
  );
}

export default App;



// // React 라이브러리에서 useEffect와 useState 훅을 불러온다.
// // - useState: 컴포넌트 내부에서 상태(값)를 저장하고 변경하기 위한 훅
// // - useEffect: 컴포넌트가 렌더링된 이후에 실행할 부수효과(side-effect) 로직을 넣기 위한 훅
// import { useEffect, useState } from "react";

// // App이라는 함수형 컴포넌트를 정의한다.
// // 이 컴포넌트가 React 앱의 최상위 컴포넌트(엔트리 포인트) 역할을 한다.
// function App() {
//   // msg라는 상태 변수와, 그 값을 변경하는 setMsg 함수를 선언한다.
//   // 초기값은 "요청 대기 중..." 이며, 화면에 "지금 서버에 요청을 보내는 중"이라는 느낌을 주는 텍스트로 사용된다.
//   const [msg, setMsg] = useState("요청 대기 중...");

//   // error라는 상태 변수와, 그 값을 변경하는 setError 함수를 선언한다.
//   // 초기값은 null 이고, 에러가 발생하지 않았다는 의미이다.
//   // 에러가 발생하면 여기 문자열(에러 메시지)을 넣어서 화면에 표시한다.
//   const [error, setError] = useState(null);

//   // useEffect 훅을 사용해서 컴포넌트가 마운트(화면에 처음 나타날 때)될 때 한 번만 실행할 로직을 정의한다.
//   // 의존성 배열(deps)을 []로 비워두었기 때문에, 이 안의 콜백은 최초 렌더링 시 단 한 번만 실행된다.
//   useEffect(() => {
//     // 비동기 요청(서버 호출)을 깔끔하게 처리하기 위해 내부에 async 함수를 정의한다.
//     // useEffect의 콜백 자체는 async로 만드는 것보다, 이렇게 내부에서 async 함수를 정의하고 호출하는 패턴이 권장된다.
//     const fetchTest = async () => {
//       try {
//         // fetch 함수를 이용해 백엔드(Spring Boot) 서버의 /api/test 엔드포인트로 HTTP GET 요청을 보낸다.
//         // URL: http://localhost:8080/api/test
//         // - localhost: 내 컴퓨터를 의미
//         // - 8080: Spring Boot 기본 포트
//         // - /api/test: 우리가 만든 테스트용 컨트롤러의 URL 경로
//         const res = await fetch("http://localhost:8080/api/test");

//         // fetch 호출 결과로 받은 응답 객체(res)의 상태 코드가 정상 범위(200~299)가 아니면 오류로 간주한다.
//         // res.ok가 false인 경우(예: 404, 500 등), 직접 Error 객체를 던져서 catch 블록에서 처리하게 만든다.
//         if (!res.ok) {
//           // new Error로 에러 객체를 생성하고, 에러 메시지에 HTTP 상태 코드를 포함한다.
//           // 예: "서버 응답 에러: 500"
//           throw new Error(`서버 응답 에러: ${res.status}`);
//         }

//         // 응답 본문을 텍스트 형태로 읽어온다.
//         // 백엔드에서 String을 리턴하도록 구현했기 때문에 res.json()이 아니라 res.text()를 사용한다.
//         const text = await res.text();

//         // 읽어온 텍스트를 msg 상태에 저장한다.
//         // 이 상태값은 아래 JSX에서 "백엔드 응답: {msg}" 부분에 표시된다.
//         setMsg(text);
//       } catch (e) {
//         // try 블록에서 에러가 발생했을 때 실행되는 부분.
//         // e는 Error 객체(또는 유사 객체)일 가능성이 크다.
//         // 에러 메시지를 error 상태에 저장해서 화면에 표시할 준비를 한다.
//         // e.message가 없는 경우를 대비하려면 추가 방어 코드도 가능하나, 여기서는 간단히 e.message만 사용한다.
//         setError(e.message);
//       }
//     };

//     // 위에서 정의한 비동기 함수 fetchTest를 실제로 호출한다.
//     // 이 호출로 인해 컴포넌트가 처음 렌더링될 때 백엔드 서버에 한 번 요청이 날아간다.
//     fetchTest();
//     // 의존성 배열을 빈 배열로 두었기 때문에, 이 useEffect는 컴포넌트 최초 마운트 시에만 실행된다.
//   }, []);

//   // 이 함수형 컴포넌트가 브라우저에 렌더링할 JSX를 반환한다.
//   // JSX는 HTML과 비슷하지만 JavaScript 안에서 작성되는 문법이다.
//   return (
//     // 최상위 <div> 요소. 인라인 스타일로 padding을 20px 주어 내용에 여백을 만든다.
//     <div style={{ padding: 20 }}>
//       {/* h1 제목 요소. 페이지 상단에 큰 제목으로 "React ↔ Spring 연동 테스트"를 출력한다. */}
//       <h1>React ↔ Spring 연동 테스트</h1>
//       {/* 삼항 연산자를 사용해 error 상태 유무에 따라 다른 내용을 조건부 렌더링 한다. */}
//       {error ? (
//         // error 값이 존재할 경우(에러가 발생한 경우) 이 <p> 요소가 렌더링된다.
//         // 스타일로 글자 색을 빨간색으로 지정하여 오류 메시임을 눈에 띄게 표시한다.
//         // "에러: {error}" 형태로, 실제 에러 메시지를 함께 보여준다.
//         <p style={{ color: "red" }}>에러: {error}</p>
//       ) : (
//         // error 값이 없을 경우(에러가 발생하지 않은 경우) 이 <p> 요소가 렌더링된다.
//         // "백엔드 응답: {msg}" 형태로, 백엔드에서 받아온 문자열(msg 상태값)을 화면에 표시한다.
//         // 초기에는 "요청 대기 중..."이 보이다가, 백엔드 응답이 오면 그 내용으로 바뀐다.
//         <p>백엔드 응답: {msg}</p>
//       )}
//     </div>
//   );
// }

// // App 컴포넌트를 모듈의 기본(default) 내보내기(export)로 지정한다.
// // 다른 파일에서 `import App from "./App";` 형태로 이 컴포넌트를 불러와 사용할 수 있다.
// export default App;