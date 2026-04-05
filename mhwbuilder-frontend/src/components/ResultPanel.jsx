import { useState } from "react";

function ResultPanel({ recommendData }) {
  const [openedCardIndex, setOpenedCardIndex] = useState(null);

  const cards = recommendData?.cards ?? [];

  const handleCardClick = (index) => {
    setOpenedCardIndex((prev) => (prev === index ? null : index));
  };

  const renderEmptyState = (message) => (
    <div className="h-full flex items-center justify-center text-sm text-gray-500">
      {message}
    </div>
  );

  const renderSkillChip = (skill, index) => (
    <div
      key={index}
      className="px-3 py-1.5 rounded-lg bg-[#222222] border border-[#303030] text-sm text-white leading-none"
    >
      {skill.skillName} <span className="text-gray-400">Lv{skill.level}</span>
    </div>
  );

  const renderDecorationChip = (deco, index) => (
    <div
      key={index}
      className="px-3.5 py-1.5 rounded-full bg-[#2a2a2a] border border-[#444444] text-sm text-white leading-none flex items-center gap-1"
    >
      {deco.name} <span className="text-gray-400">x{deco.count}</span>
    </div>
  );

  const renderStatRow = (label, value) => (
    <div className="bg-[#131313] rounded-lg border border-[#252525] px-3 py-2 text-sm text-[#e5e2e1] flex items-center justify-between">
      <span className="text-gray-300">{label}</span>
      <span className="font-medium text-white">{value}</span>
    </div>
  );

  return (
    <div className="w-full h-full min-w-0 bg-[#171717] rounded-2xl border border-[#2a2a2a] overflow-hidden flex flex-col">
      <div className="px-5 py-4 border-b border-[#2a2a2a]">
        <h2 className="text-white text-lg font-semibold">추천 결과</h2>
        <p className="text-sm text-gray-400 mt-1">
          조건에 맞는 세팅 결과를 확인할 수 있습니다.
        </p>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {!recommendData && renderEmptyState("아직 결과 없음")}
        {recommendData && cards.length === 0 && renderEmptyState("추천 결과가 비어 있습니다.")}

        {cards.map((card, index) => {
          const isOpen = openedCardIndex === index;

          const equips = card.detail?.equips ?? [];
          const decorations = card.detail?.decorations ?? [];
          const finalSkills = card.detail?.finalSkills ?? [];
          const stats = card.detail?.stats;

          const previewSkills = finalSkills.slice(0, 6);
          const hiddenSkillCount = finalSkills.length - previewSkills.length;

          return (
            <div
              key={index}
              onClick={() => handleCardClick(index)}
              className={`rounded-2xl border cursor-pointer transition-all ${
                isOpen
                  ? "bg-[#101010] border-[#3a3a3a]"
                  : "bg-[#111111] border-[#2a2a2a] hover:border-[#353535]"
              }`}
            >
              <div className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="text-white text-base font-semibold break-words">
                        {card.title}
                      </h3>
                    </div>

                    {!isOpen && (
                      <div className="mt-3 space-y-3">
                        <div className="flex flex-wrap gap-2">
                          {previewSkills.length > 0 ? (
                            <>
                              {previewSkills.map(renderSkillChip)}
                              {hiddenSkillCount > 0 && (
                                <div className="px-3 py-1.5 rounded-lg bg-[#1c1c1c] border border-[#2b2b2b] text-sm text-gray-400 leading-none">
                                  +{hiddenSkillCount}개
                                </div>
                              )}
                            </>
                          ) : (
                            <div className="text-sm text-gray-500">
                              표시할 스킬 정보가 없습니다.
                            </div>
                          )}
                        </div>

                        {card.summary && (
                          <p className="text-sm text-gray-400 leading-6 break-words">
                            {card.summary}
                          </p>
                        )}
                      </div>
                    )}
                  </div>

                  <div className="shrink-0 text-xs text-gray-500 pt-1">
                    {isOpen ? "접기" : "상세"}
                  </div>
                </div>

                {isOpen && (
                  <div className="mt-4 pt-4 border-t border-[#222222] space-y-4">
                    <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
                      {/* 장비 */}
                      <section className="bg-[#1b1b1b] rounded-xl p-4 border border-[#2a2a2a] min-w-0">
                        <div className="text-sm font-medium text-gray-200 mb-3">
                          장비 구성
                        </div>

                        {equips.length > 0 ? (
                          <div className="space-y-2">
                            {equips.map((equip, equipIndex) => (
                              <div
                                key={equipIndex}
                                className="px-3 py-2 rounded-lg bg-[#131313] border border-[#252525] text-sm text-white break-words"
                              >
                                {equip.name}
                              </div>
                            ))}
                          </div>
                        ) : (
                          <div className="text-sm text-gray-500">
                            장비 정보가 없습니다.
                          </div>
                        )}
                      </section>

                      {/* 스탯 */}
                      <section className="bg-[#1b1b1b] rounded-xl p-4 border border-[#2a2a2a] min-w-0">
                        <div className="text-sm font-medium text-gray-200 mb-3">
                          스탯
                        </div>

                        {stats ? (
                          <div className="space-y-3">
                            <div className="bg-[#131313] rounded-xl border border-[#252525] px-4 py-4 flex flex-col items-center justify-center">
                              <p className="text-[11px] text-gray-400 tracking-wide mb-1">
                                TOTAL DEFENSE
                              </p>
                              <p className="text-2xl font-bold text-white">
                                {stats.defense}
                              </p>
                            </div>

                            <div className="grid grid-cols-2 gap-2">
                              {renderStatRow("화", stats.fireRes)}
                              {renderStatRow("수", stats.waterRes)}
                              {renderStatRow("뇌", stats.thunderRes)}
                              {renderStatRow("빙", stats.iceRes)}
                              <div className="col-span-2">
                                {renderStatRow("용", stats.dragonRes)}
                              </div>
                            </div>
                          </div>
                        ) : (
                          <div className="text-sm text-gray-500">
                            스탯 정보가 없습니다.
                          </div>
                        )}
                      </section>

                      {/* 장식주 */}
                      <section className="bg-[#1b1b1b] rounded-xl p-4 border border-[#2a2a2a] min-w-0">
                        <div className="text-sm font-medium text-gray-200 mb-3">
                          장식주
                        </div>

                        {decorations.length > 0 ? (
                          <div className="flex flex-wrap gap-2">
                            {decorations.map(renderDecorationChip)}
                          </div>
                        ) : (
                          <div className="text-sm text-gray-500">
                            장식주 정보가 없습니다.
                          </div>
                        )}
                      </section>
                    </div>

                    {/* 최종 스킬 */}
                    <section className="bg-[#1b1b1b] rounded-xl p-4 border border-[#2a2a2a]">
                      <div className="text-sm font-medium text-gray-200 mb-3">
                        최종 스킬
                      </div>

                      {finalSkills.length > 0 ? (
                        <div className="flex flex-wrap gap-2">
                          {finalSkills.map(renderSkillChip)}
                        </div>
                      ) : (
                        <div className="text-sm text-gray-500">
                          최종 스킬 정보가 없습니다.
                        </div>
                      )}
                    </section>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ResultPanel;