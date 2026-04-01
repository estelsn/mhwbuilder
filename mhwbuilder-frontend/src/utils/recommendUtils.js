export const MIN_NORMAL_SKILL_VALUE = 20;

const NORMAL_TYPE = "normal";
const SERIES_TYPE = "series";
const GROUP_TYPE = "group";

function isNormalSkillTag(tag) {
  return tag?.type === NORMAL_TYPE;
}

function isSetSkillTag(tag) {
  return tag?.type === SERIES_TYPE || tag?.type === GROUP_TYPE;
}

/**
 * 일반 스킬 1개 밸류 계산
 * 공식:
 * targetLevel * decorationSlotLevel
 */
export function calculateNormalSkillValue(tag) {
  if (!isNormalSkillTag(tag)) {
    return 0;
  }

  const targetLevel = Number(tag.targetLevel ?? 0);
  const decorationSlotLevel = Number(tag.decorationSlotLevel ?? 0);

  if (targetLevel <= 0 || decorationSlotLevel <= 0) {
    return 0;
  }

  return targetLevel * decorationSlotLevel;
}

/**
 * 전체 일반 스킬 밸류 합산
 */
export function calculateTotalNormalSkillValue(selectedTags = []) {
  return selectedTags
    .filter(isNormalSkillTag)
    .reduce((sum, tag) => sum + calculateNormalSkillValue(tag), 0);
}

/**
 * selectedTags -> RecommendRequest 변환
 *
 * 백엔드 요청 형태:
 * {
 *   normalSkills: [{ skillId, targetLevel }],
 *   setSkills: [{ setSkillId, requiredCount }]
 * }
 */
export function buildRecommendRequest(selectedTags = []) {
  const normalSkills = selectedTags
    .filter(isNormalSkillTag)
    .map((tag) => ({
      skillId: tag.id,
      targetLevel: Number(tag.targetLevel ?? 0),
    }))
    .filter((skill) => skill.skillId && skill.targetLevel > 0);

  const setSkills = selectedTags
    .filter(isSetSkillTag)
    .map((tag) => ({
      setSkillId: tag.id,
      requiredCount: Number(tag.selectedRequiredCount ?? 0),
    }))
    .filter((skill) => skill.setSkillId && skill.requiredCount > 0);

  return {
    normalSkills,
    setSkills,
  };
}

/**
 * 추천 실행 가능 여부 검사
 *
 * 조건:
 * 1. 세트 스킬 1개 이상 필수
 * 2. 일반 스킬 총 밸류 20 이상 필수
 */
export function validateRecommendationInput(selectedTags = []) {
  const setSkillCount = selectedTags.filter(isSetSkillTag).length;
  const totalNormalSkillValue = calculateTotalNormalSkillValue(selectedTags);

  const hasSetSkill = setSkillCount >= 1;
  const hasEnoughNormalSkillValue =
    totalNormalSkillValue >= MIN_NORMAL_SKILL_VALUE;

  if (!hasSetSkill && !hasEnoughNormalSkillValue) {
    return {
      valid: false,
      message:
        "시리즈/그룹 스킬을 최소 1개 이상 선택하고, 일반 스킬 총 밸류를 20 이상으로 맞춰주세요.",
      totalNormalSkillValue,
      setSkillCount,
    };
  }

  if (!hasSetSkill) {
    return {
      valid: false,
      message: "시리즈 스킬 또는 그룹 스킬을 최소 1개 이상 선택해주세요.",
      totalNormalSkillValue,
      setSkillCount,
    };
  }

  if (!hasEnoughNormalSkillValue) {
    return {
      valid: false,
      message:
        "일반 스킬 조건이 너무 적습니다. 총 스킬 밸류 20 이상이 되도록 선택해주세요.",
      totalNormalSkillValue,
      setSkillCount,
    };
  }

  return {
    valid: true,
    message: null,
    totalNormalSkillValue,
    setSkillCount,
  };
}

export function canStartRecommendation(selectedTags = []) {
  return validateRecommendationInput(selectedTags).valid;
}