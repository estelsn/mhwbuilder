import { useState } from "react";

function ResultPanel({ recommendData }) {
  const [openedCardIndex, setOpenedCardIndex] = useState(null);

  const cards = recommendData?.cards ?? [];

  const handleCardClick = (index) => {
    setOpenedCardIndex((prev) => (prev === index ? null : index));
  };

  return (
    <div className="w-full h-full min-w-0 bg-[#171717] rounded-2xl border border-[#2a2a2a] overflow-hidden flex flex-col">
      <div className="px-5 py-4 border-b border-[#2a2a2a]">
        <h2 className="text-white text-lg font-semibold">추천 결과</h2>
        <p className="text-sm text-gray-400 mt-1">
          추천된 세팅 결과를 확인할 수 있습니다.
        </p>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {!recommendData && (
          <div className="h-full flex items-center justify-center text-sm text-gray-500">
            아직 결과 없음
          </div>
        )}

        {recommendData && cards.length === 0 && (
          <div className="h-full flex items-center justify-center text-sm text-gray-500">
            추천 결과가 비어 있습니다.
          </div>
        )}

        {cards.map((card, index) => {
          const isOpen = openedCardIndex === index;

          const equips = card.detail?.equips ?? [];
          const decorations = card.detail?.decorations ?? [];
          const finalSkills = card.detail?.finalSkills ?? [];
          const stats = card.detail?.stats;

          return (
            <div
              key={index}
              onClick={() => handleCardClick(index)}
              className="bg-[#111111] border border-[#2a2a2a] rounded-2xl p-4 cursor-pointer transition hover:border-[#3a3a3a]"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <h3 className="text-white text-base font-semibold">
                    {card.title}
                  </h3>

                  {!isOpen && (
                    <div className="flex flex-wrap gap-2 mt-3">
                      {finalSkills.length > 0 ? (
                        finalSkills.map((skill, skillIndex) => (
                          <div
                            key={skillIndex}
                            className="px-3 py-1.5 rounded-full bg-[#252525] border border-[#333333] text-sm text-white"
                          >
                            {skill.skillName} Lv{skill.level}
                          </div>
                        ))
                      ) : (
                        <div className="text-sm text-gray-500">
                          표시할 스킬 정보가 없습니다.
                        </div>
                      )}
                    </div>
                  )}
                </div>

                <div className="text-xs text-gray-500 shrink-0 pt-1">
                  {isOpen ? "접기" : "상세보기"}
                </div>
              </div>

              {isOpen && (
                <div className="mt-4 pt-4 border-t border-[#222222]">
                  <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
                    {/* 방어구 */}
                    <div className="bg-[#1b1b1b] rounded-xl p-3 border border-[#2a2a2a] min-w-0">
                      <div className="text-sm font-medium text-gray-300 mb-3">
                        방어구
                      </div>

                      <div className="space-y-2">
                        {equips.length > 0 ? (
                          equips.map((equip, equipIndex) => (
                            <div
                              key={equipIndex}
                              className="text-sm text-white break-words leading-6"
                            >
                              {equip.name}
                            </div>
                          ))
                        ) : (
                          <div className="text-sm text-gray-500">
                            장비 정보가 없습니다.
                          </div>
                        )}
                      </div>
                    </div>

                    {/* 스탯 */}
                    <div className="bg-[#1b1b1b] rounded-xl p-3 border border-[#2a2a2a] min-w-0">
                      <div className="text-sm font-medium text-gray-300 mb-3">
                        스탯
                      </div>

                      {stats ? (
                        <div className="space-y-3">
                          <div className="bg-[#131313] p-4 rounded-lg flex flex-col justify-center items-center border border-[#2a2a2a]">
                            <p className="text-[10px] text-[#a48c7a] uppercase tracking-widest mb-1">
                              Total Defense
                            </p>
                            <p className="text-2xl font-bold text-[#ffb97c]">
                              {stats.defense}
                            </p>
                          </div>

                          <div className="grid grid-cols-2 gap-2">
                            <div className="bg-[#131313] rounded-lg border border-[#2a2a2a] px-3 py-2 text-sm text-[#e5e2e1] flex justify-between">
                              <span>화</span>
                              <span>{stats.fireRes}</span>
                            </div>
                            <div className="bg-[#131313] rounded-lg border border-[#2a2a2a] px-3 py-2 text-sm text-[#e5e2e1] flex justify-between">
                              <span>수</span>
                              <span>{stats.waterRes}</span>
                            </div>
                            <div className="bg-[#131313] rounded-lg border border-[#2a2a2a] px-3 py-2 text-sm text-[#e5e2e1] flex justify-between">
                              <span>뇌</span>
                              <span>{stats.thunderRes}</span>
                            </div>
                            <div className="bg-[#131313] rounded-lg border border-[#2a2a2a] px-3 py-2 text-sm text-[#e5e2e1] flex justify-between">
                              <span>빙</span>
                              <span>{stats.iceRes}</span>
                            </div>
                            <div className="bg-[#131313] rounded-lg border border-[#2a2a2a] px-3 py-2 text-sm text-[#e5e2e1] flex justify-between col-span-2">
                              <span>용</span>
                              <span>{stats.dragonRes}</span>
                            </div>
                          </div>
                        </div>
                      ) : (
                        <div className="text-sm text-gray-500">
                          스탯 정보가 없습니다.
                        </div>
                      )}
                    </div>

                    {/* 장식주 */}
                    <div className="bg-[#1b1b1b] rounded-xl p-3 border border-[#2a2a2a] min-w-0">
                      <div className="text-sm font-medium text-gray-300 mb-3">
                        장식주
                      </div>

                      {decorations.length > 0 ? (
                        <div className="flex flex-wrap gap-2">
                          {decorations.map((deco, decoIndex) => (
                            <div
                              key={decoIndex}
                              className="px-2.5 py-1 rounded-full bg-[#252525] border border-[#333333] text-sm text-white"
                            >
                              {deco.name}{" "}
                              <span className="text-gray-400">x{deco.count}</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-sm text-gray-500">
                          장식주 정보가 없습니다.
                        </div>
                      )}
                    </div>
                  </div>

                  {/* 최종 스킬 */}
                  <div className="mt-4 bg-[#1b1b1b] rounded-xl p-3 border border-[#2a2a2a]">
                    <div className="text-sm font-medium text-gray-300 mb-3">
                      최종 스킬
                    </div>

                    <div className="flex flex-wrap gap-2">
                      {finalSkills.length > 0 ? (
                        finalSkills.map((skill, skillIndex) => (
                          <div
                            key={skillIndex}
                            className="px-3 py-1.5 rounded-full bg-[#252525] border border-[#333333] text-sm text-white"
                          >
                            {skill.skillName} Lv{skill.level}
                          </div>
                        ))
                      ) : (
                        <div className="text-sm text-gray-500">
                          최종 스킬 정보가 없습니다.
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ResultPanel;