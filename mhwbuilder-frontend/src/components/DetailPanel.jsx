function DetailPanel() {
  return (
    <aside className="w-[360px] bg-[#1c1b1b] border-l border-[#2a2a2a] shrink-0 overflow-y-auto">
      <div className="p-6">
        <h3 className="text-lg font-bold tracking-tight mb-6 text-[#e5e2e1]">
          상세 정보
        </h3>

        <div className="grid grid-cols-2 gap-3 mb-6">
          <div className="bg-[#131313] p-4 rounded-lg flex flex-col justify-center items-center border border-[#2a2a2a]">
            <p className="text-[10px] text-[#a48c7a] uppercase tracking-widest mb-1">
              Total Defense
            </p>
            <p className="text-2xl font-bold text-[#ffb97c]">-</p>
          </div>

          <div className="bg-[#131313] p-4 rounded-lg flex items-center justify-center border border-[#2a2a2a]">
            <p className="text-xs text-[#a48c7a]">선택된 결과 없음</p>
          </div>
        </div>

        <div className="bg-[#131313] border border-[#2a2a2a] rounded-lg p-4 text-sm text-[#a48c7a]">
          추천 결과 카드를 클릭하면 여기에 상세 장비 정보가 표시됩니다.
        </div>
      </div>
    </aside>
  );
}

export default DetailPanel;