import { useEffect, useState } from "react";
import {
  buildRecommendRequest,
  calculateTotalNormalSkillValue,
  validateRecommendationInput,
} from "../utils/recommendUtils";

function LeftPanel({ onRecommend = () => {} }) {
  const [skillData, setSkillData] = useState({
    normalSkills: [],
    seriesSkills: [],
    groupSkills: [],
  });

  const [selectedTags, setSelectedTags] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/api/skills/select-page")
      .then((res) => {
        if (!res.ok) {
          throw new Error("스킬 데이터를 불러오지 못했습니다.");
        }
        return res.json();
      })
      .then((data) => {
        setSkillData(data);
      })
      .catch((err) => {
        alert("스킬 데이터를 불러오는 중 오류가 발생했습니다.");
      });
  }, []);

  const addNormalSkill = (skillId) => {
    if (!skillId) return;

    const skill = skillData.normalSkills.find(
      (item) => item.skillId === skillId
    );
    if (!skill) return;

    const alreadyExists = selectedTags.some(
      (tag) => tag.type === "normal" && tag.id === skill.skillId
    );
    if (alreadyExists) return;

    setSelectedTags((prev) => [
      ...prev,
      {
        id: skill.skillId,
        type: "normal",
        name: skill.skillName,
        targetLevel: 1,
        maxLevel: skill.maxLevel,
        decorationSlotLevel: skill.decorationSlotLevel ?? 0,
      },
    ]);
  };

  const addSetSkill = (setSkillId, type) => {
    if (!setSkillId) return;

    const source =
      type === "series" ? skillData.seriesSkills : skillData.groupSkills;

    const skill = source.find((item) => item.setSkillId === setSkillId);
    if (!skill) return;

    const alreadyExists = selectedTags.some(
      (tag) => tag.type === type && tag.id === skill.setSkillId
    );
    if (alreadyExists) return;

    const effects = skill.effects ?? [];
    const firstEffect = effects[0];

    setSelectedTags((prev) => [
      ...prev,
      {
        id: skill.setSkillId,
        type,
        name: skill.setSkillName,
        selectedRequiredCount: firstEffect?.requiredCount ?? 0,
        effects,
      },
    ]);
  };

  const removeTag = (id, type) => {
    setSelectedTags((prev) =>
      prev.filter((tag) => !(tag.id === id && tag.type === type))
    );
  };

  const increaseLevel = (id) => {
    setSelectedTags((prev) =>
      prev.map((tag) => {
        if (tag.type !== "normal" || tag.id !== id) return tag;
        if (tag.targetLevel >= tag.maxLevel) return tag;
        return { ...tag, targetLevel: tag.targetLevel + 1 };
      })
    );
  };

  const decreaseLevel = (id) => {
    setSelectedTags((prev) =>
      prev.map((tag) => {
        if (tag.type !== "normal" || tag.id !== id) return tag;
        if (tag.targetLevel <= 1) return tag;
        return { ...tag, targetLevel: tag.targetLevel - 1 };
      })
    );
  };

  const changeSetEffect = (id, type, requiredCount) => {
    setSelectedTags((prev) =>
      prev.map((tag) => {
        if (tag.id !== id || tag.type !== type) return tag;
        return {
          ...tag,
          selectedRequiredCount: Number(requiredCount),
        };
      })
    );
  };

  const totalNormalSkillValue = calculateTotalNormalSkillValue(selectedTags);

  const handleStartRecommendation = async () => {
    const validation = validateRecommendationInput(selectedTags);

    if (!validation.valid) {
      alert(validation.message);
      return;
    }

    const requestBody = buildRecommendRequest(selectedTags);

    try {
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

      const data = await response.json();

      onRecommend(data);
      alert("추천 요청 성공");
    } catch (error) {
      alert(error.message || "추천 요청 중 오류가 발생했습니다.");
    }
  };

  const resetFilters = () => {
    setSelectedTags([]);
  };

  return (
    <aside className="w-[360px] bg-[#1c1b1b] border-r border-[#2a2a2a] shrink-0 overflow-hidden">
      <div className="p-5 flex flex-col h-full">
        <h2 className="text-lg font-bold tracking-tight mb-6 text-[#e5e2e1]">
          스킬 선택
        </h2>

        <div className="space-y-5 flex-grow overflow-y-auto pr-1">
          <div className="space-y-2">
            <label className="block text-[11px] font-bold tracking-widest text-[#dcc2ae] uppercase">
              Series Skill
            </label>
            <select
              className="w-full bg-[#2a2a2a] text-[#e5e2e1] rounded-lg py-2.5 px-3 text-sm"
              defaultValue=""
              onChange={(e) => {
                addSetSkill(e.target.value, "series");
                e.target.value = "";
              }}
            >
              <option value="">Select Series Skill...</option>
              {skillData.seriesSkills.map((skill) => (
                <option key={skill.setSkillId} value={skill.setSkillId}>
                  {skill.setSkillName}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="block text-[11px] font-bold tracking-widest text-[#dcc2ae] uppercase">
              Group Skill
            </label>
            <select
              className="w-full bg-[#2a2a2a] text-[#e5e2e1] rounded-lg py-2.5 px-3 text-sm"
              defaultValue=""
              onChange={(e) => {
                addSetSkill(e.target.value, "group");
                e.target.value = "";
              }}
            >
              <option value="">Select Group Skill...</option>
              {skillData.groupSkills.map((skill) => (
                <option key={skill.setSkillId} value={skill.setSkillId}>
                  {skill.setSkillName}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label className="block text-[11px] font-bold tracking-widest text-[#dcc2ae] uppercase">
              General Skill
            </label>
            <select
              className="w-full bg-[#2a2a2a] text-[#e5e2e1] rounded-lg py-2.5 px-3 text-sm"
              defaultValue=""
              onChange={(e) => {
                addNormalSkill(e.target.value);
                e.target.value = "";
              }}
            >
              <option value="">Search Skills...</option>
              {skillData.normalSkills.map((skill) => (
                <option key={skill.skillId} value={skill.skillId}>
                  {skill.skillName} (최대 {skill.maxLevel}, 슬롯 {skill.decorationSlotLevel ?? "없음"})
                </option>
              ))}
            </select>
          </div>

          <div className="mt-6">
            <label className="block text-[11px] font-bold tracking-widest text-[#dcc2ae] uppercase mb-3">
              Active Requirements
            </label>

            <div className="grid grid-cols-2 gap-2">
              {selectedTags.length === 0 && (
                <div className="col-span-2 text-sm text-[#a48c7a]">
                  선택된 스킬이 없습니다.
                </div>
              )}

              {selectedTags.map((tag) => {
                const selectedEffect =
                  tag.effects?.find(
                    (effect) => effect.requiredCount === tag.selectedRequiredCount
                  ) ?? null;

                return (
                  <div
                    key={`${tag.type}-${tag.id}`}
                    className="relative bg-[#353534] p-3 rounded border-l-2 border-[#ff9100] min-h-[88px]"
                  >
                    <p className="text-sm font-medium text-[#e5e2e1] mb-2 pr-4">
                      {tag.name}
                    </p>

                    {tag.type === "normal" ? (
                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => decreaseLevel(tag.id)}
                          className="w-5 h-5 text-xs flex items-center justify-center bg-[#1c1b1b] hover:bg-[#2a2a2a] text-white rounded"
                        >
                          -
                        </button>
                        <p className="text-[10px] text-[#dcc2ae] uppercase tracking-tight min-w-[64px] text-center">
                          Lv {tag.targetLevel}/{tag.maxLevel}
                        </p>
                        <button
                          onClick={() => increaseLevel(tag.id)}
                          className="w-5 h-5 text-xs flex items-center justify-center bg-[#1c1b1b] hover:bg-[#2a2a2a] text-white rounded"
                        >
                          +
                        </button>
                      </div>
                    ) : (
                      <div className="space-y-1">
                        <select
                          className="w-full bg-[#1c1b1b] text-[#e5e2e1] rounded px-2 py-1 text-[11px]"
                          value={tag.selectedRequiredCount}
                          onChange={(e) =>
                            changeSetEffect(tag.id, tag.type, e.target.value)
                          }
                        >
                          {tag.effects?.map((effect) => (
                            <option
                              key={effect.requiredCount}
                              value={effect.requiredCount}
                            >
                              {effect.requiredCount}세트
                            </option>
                          ))}
                        </select>

                        <p className="text-[10px] text-[#dcc2ae] leading-tight">
                          {selectedEffect?.effectName ?? ""}
                        </p>
                      </div>
                    )}

                    <button
                      onClick={() => removeTag(tag.id, tag.type)}
                      className="absolute top-1 right-1 text-[#a48c7a] hover:text-red-400 text-[11px] leading-none"
                    >
                      x
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
          <div className="text-xs text-[#a48c7a] mt-2">
            현재 일반 스킬 총 밸류: {totalNormalSkillValue}
          </div>
        </div>

        <div className="mt-auto pt-5 space-y-2">
          <button
            onClick={handleStartRecommendation}
            className="w-full bg-gradient-to-r from-[#ffb97c] to-[#ff9100] text-[#4c2700] font-bold py-3 rounded shadow-lg text-sm"
          >
            START RECOMMENDATION
          </button>
          <button
            onClick={resetFilters}
            className="w-full bg-transparent border border-[#564334] text-[#e5e2e1] font-medium py-2.5 rounded hover:bg-[#353534] text-sm"
          >
            RESET FILTERS
          </button>
        </div>
      </div>
    </aside>
  );
}

export default LeftPanel;