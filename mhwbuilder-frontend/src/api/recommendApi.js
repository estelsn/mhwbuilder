export async function requestRecommendation(requestBody) {
  const response = await fetch("http://localhost:8080/api/recommend", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "추천 요청에 실패했습니다.");
  }

  return response.json();
}