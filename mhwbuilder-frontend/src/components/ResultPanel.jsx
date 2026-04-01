function ResultPanel() {
  return (
    <section className="flex-1 bg-[#131313] overflow-y-auto p-8">
      <header className="mb-8">
        <h1 className="text-3xl font-extrabold tracking-tight text-[#e5e2e1] mb-2">
          추천 결과
        </h1>
        <p className="text-[#a48c7a] text-sm">
          조건을 선택한 뒤 추천 버튼을 누르면 결과가 표시됩니다.
        </p>
      </header>

      <div className="bg-[#1c1b1b] border border-[#2a2a2a] rounded-lg p-6 text-[#a48c7a] text-sm">
        아직 추천 결과가 없습니다.
      </div>
    </section>
  );
}

export default ResultPanel;