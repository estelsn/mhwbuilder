import { useEffect, useRef, useState } from "react";
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
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState({
    visible: false,
    type: "success",
    message: "",
  });

  const toastTimerRef = useRef(null);

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
      .catch(() => {
        alert("스킬 데이터를 불러오는 중 오류가 발생했습니다.");
      });
  }, []);

  useEffect(() => {
    return () => {
      if (toastTimerRef.current) {
        clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  const showToast = (message, type = "success") => {
    if (toastTimerRef.current) {
      clearTimeout(toastTimerRef.current);
    }

    setToast({
      visible: true,
      type,
      message,
    });

    toastTimerRef.current = setTimeout(() => {
      setToast((prev) => ({
        ...prev,
        visible: false,
      }));
    }, 2200);
  };

  const addNormalSkill = (skillId) => {
    if (!skillId) return;

    const skill = skillData.normalSkills.find((item) => item.skillId === skillId);
    if (!skill) return;

    const exists = selectedTags.some(
      (tag) => tag.type === "normal" && tag.id === skill.skillId
    );
    if (exists) return;

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

    const targetList =
      type === "series" ? skillData.seriesSkills : skillData.groupSkills;

    const skill = targetList.find((item) => item.setSkillId === setSkillId);
    if (!skill) return;

    const exists = selectedTags.some(
      (tag) => tag.type === type && tag.id === skill.setSkillId
    );
    if (exists) return;

    const firstEffect = skill.effects?.[0];

    setSelectedTags((prev) => [
      ...prev,
      {
        id: skill.setSkillId,
        type,
        name: skill.setSkillName,
        selectedRequiredCount: firstEffect?.requiredCount ?? 0,
        effects: skill.effects ?? [],
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

  const handleStartRecommendation = async () => {
    if (isSubmitting) return;

    const validation = validateRecommendationInput(selectedTags);

    if (!validation.valid) {
      alert(validation.message);
      return;
    }

    const requestBody = buildRecommendRequest(selectedTags);

    try {
      setIsSubmitting(true);

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
      showToast("추천 결과를 불러왔습니다.");
    } catch (error) {
      alert(error.message || "추천 요청 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetFilters = () => {
    if (isSubmitting) return;
    setSelectedTags([]);
    showToast("선택한 스킬을 초기화했습니다.");
  };

  const setSkillTags = selectedTags.filter(
    (tag) => tag.type === "series" || tag.type === "group"
  );
  const normalSkillTags = selectedTags.filter((tag) => tag.type === "normal");

  const totalNormalSkillValue = calculateTotalNormalSkillValue(selectedTags);

  return (
    <aside className="w-[360px] bg-[#1c1b1b] border-r border-[#2a2a2a] shrink-0 overflow-hidden">
      <div className="relative flex h-full flex-col p-5">
        <div
          className={`absolute left-5 right-5 top-5 z-20 transition-all duration-200 ${
            toast.visible
              ? "translate-y-0 opacity-100"
              : "-translate-y-1 opacity-0 pointer-events-none"
          }`}
        >
          <div
            className={`rounded-lg border px-3 py-2 text-sm shadow-lg ${
              toast.type === "success"
                ? "border-[#5d4736] bg-[#2a241f] text-[#f3dfcf]"
                : "border-[#6a3b3b] bg-[#2a1f1f] text-[#f1c7c7]"
            }`}
          >
            {toast.message}
          </div>
        </div>

        <h2 className="mb-5 text-lg font-bold tracking-tight text-[#e5e2e1]">
          스킬 선택
        </h2>

        <div className="flex-grow overflow-y-auto pr-1">
          <div className="space-y-3">
            <select
              className="w-full rounded-lg border border-[#343434] bg-[#262626] px-3 py-2.5 text-sm text-[#e5e2e1] outline-none transition focus:border-[#6f5a48]"
              defaultValue=""
              onChange={(e) => {
                addSetSkill(e.target.value, "series");
                e.target.value = "";
              }}
            >
              <option value="">시리즈 스킬 선택</option>
              {skillData.seriesSkills.map((skill) => (
                <option key={skill.setSkillId} value={skill.setSkillId}>
                  {skill.setSkillName}
                </option>
              ))}
            </select>

            <select
              className="w-full rounded-lg border border-[#343434] bg-[#262626] px-3 py-2.5 text-sm text-[#e5e2e1] outline-none transition focus:border-[#6f5a48]"
              defaultValue=""
              onChange={(e) => {
                addSetSkill(e.target.value, "group");
                e.target.value = "";
              }}
            >
              <option value="">그룹 스킬 선택</option>
              {skillData.groupSkills.map((skill) => (
                <option key={skill.setSkillId} value={skill.setSkillId}>
                  {skill.setSkillName}
                </option>
              ))}
            </select>

            <select
              className="w-full rounded-lg border border-[#343434] bg-[#262626] px-3 py-2.5 text-sm text-[#e5e2e1] outline-none transition focus:border-[#6f5a48]"
              defaultValue=""
              onChange={(e) => {
                addNormalSkill(e.target.value);
                e.target.value = "";
              }}
            >
              <option value="">일반 스킬 선택</option>
              {skillData.normalSkills.map((skill) => (
                <option key={skill.skillId} value={skill.skillId}>
                  {skill.skillName} (최대 {skill.maxLevel}, 슬롯{" "}
                  {skill.decorationSlotLevel ?? "없음"})
                </option>
              ))}
            </select>
          </div>

          <div className="mt-6">
            <div className="mb-3 flex items-end justify-between">
              <div className="text-sm font-semibold text-[#e5e2e1]">
                선택된 스킬
              </div>
              <div className="text-xs text-[#a48c7a]">
                일반 스킬 밸류: {totalNormalSkillValue}
              </div>
            </div>

            {selectedTags.length === 0 && (
              <div className="rounded-lg border border-dashed border-[#3b342f] bg-[#211f1e] px-3 py-4 text-sm text-[#a48c7a]">
                아직 선택된 스킬이 없습니다.
              </div>
            )}

            {setSkillTags.length > 0 && (
              <div className="mb-4">
                <div className="mb-2 text-[11px] font-semibold tracking-wide text-[#b89a84]">
                  시리즈 / 그룹
                </div>

                <div className="grid grid-cols-2 gap-2">
                  {setSkillTags.map((tag) => {
                    const selectedEffect =
                      tag.effects?.find(
                        (effect) =>
                          effect.requiredCount === tag.selectedRequiredCount
                      ) ?? null;

                    return (
                      <div
                        key={`${tag.type}-${tag.id}`}
                        className="relative min-h-[88px] rounded bg-[#353534] p-3 border-l-2 border-[#ff9100]"
                      >
                        <p className="mb-2 pr-4 text-sm font-medium text-[#e5e2e1]">
                          {tag.name}
                        </p>

                        <div className="space-y-1">
                          <select
                            className="w-full rounded bg-[#1c1b1b] px-2 py-1 text-[11px] text-[#e5e2e1]"
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

                          <p className="text-[10px] leading-tight text-[#dcc2ae]">
                            {selectedEffect?.effectName ?? ""}
                          </p>
                        </div>

                        <button
                          onClick={() => removeTag(tag.id, tag.type)}
                          className="absolute right-1 top-1 text-[16px] leading-none text-[#a48c7a] hover:text-red-400"
                        >
                          ×
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {normalSkillTags.length > 0 && (
              <div>
                <div className="mb-2 text-[11px] font-semibold tracking-wide text-[#b89a84]">
                  일반 스킬
                </div>

                <div className="grid grid-cols-2 gap-2">
                  {normalSkillTags.map((tag) => (
                    <div
                      key={`${tag.type}-${tag.id}`}
                      className="relative min-h-[88px] rounded bg-[#353534] p-3 border-l-2 border-[#ff9100]"
                    >
                      <p className="mb-2 pr-4 text-sm font-medium text-[#e5e2e1]">
                        {tag.name}
                      </p>

                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => decreaseLevel(tag.id)}
                          className="flex h-5 w-5 items-center justify-center rounded bg-[#1c1b1b] text-xs text-white hover:bg-[#2a2a2a]"
                        >
                          -
                        </button>

                        <p className="min-w-[64px] text-center text-[10px] tracking-tight text-[#dcc2ae]">
                          Lv {tag.targetLevel}/{tag.maxLevel}
                        </p>

                        <button
                          onClick={() => increaseLevel(tag.id)}
                          className="flex h-5 w-5 items-center justify-center rounded bg-[#1c1b1b] text-xs text-white hover:bg-[#2a2a2a]"
                        >
                          +
                        </button>
                      </div>

                      <button
                        onClick={() => removeTag(tag.id, tag.type)}
                        className="absolute right-1 top-1 text-[16px] leading-none text-[#a48c7a] hover:text-red-400"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="mt-auto space-y-2 pt-5">
          <button
            onClick={handleStartRecommendation}
            disabled={isSubmitting}
            className={`w-full rounded py-3 text-sm font-bold shadow-lg transition ${
              isSubmitting
                ? "cursor-not-allowed bg-[#6f573f] text-[#d8c1aa]"
                : "bg-gradient-to-r from-[#ffb97c] to-[#ff9100] text-[#4c2700]"
            }`}
          >
            {isSubmitting ? "추천 실행 중" : "추천 실행"}
          </button>

          <button
            onClick={resetFilters}
            disabled={isSubmitting}
            className={`w-full rounded border py-2.5 text-sm font-medium transition ${
              isSubmitting
                ? "cursor-not-allowed border-[#3f342d] bg-transparent text-[#8d7b6f]"
                : "border-[#564334] bg-transparent text-[#e5e2e1] hover:bg-[#353534]"
            }`}
          >
            초기화
          </button>
        </div>
      </div>
    </aside>
  );
}

export default LeftPanel;