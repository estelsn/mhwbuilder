import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
//전체 애플리케이션의 입구. 브라우저의 실제 HTML 파일(index.html)과 리액트 코드를 연결해 주는 다리 역할. ReactDOM.createRoot를 통해 최상위 컴포넌트인 App을 화면에 렌더링


createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
