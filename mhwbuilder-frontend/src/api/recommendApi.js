export async function requestRecommendation(requestBody) {
  const response = await fetch("/api/recommend", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  });

  if (!response.ok) {
    let errorMessage = "추천 요청에 실패했습니다.";

    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      // JSON 파싱 실패 시 기본 메시지 유지
    }

    throw new Error(errorMessage);
  }

  return response.json();
}