// Respuestas y peticiones de la API ORLO para la web de usuarios (mismos nombres que los DTO).

export interface Me {
  uid: string;
  premium: boolean;
  dailyLimit: number;
  remainingMessages: number;
}

export interface StyleDto {
  id: number;
  code: string;
  name: string;
  description: string;
  palette: string[];
  gallery: string[];
}

export interface QuizOptionDto {
  id: number;
  text: string;
  imageUrl: string | null;
}

export interface QuizQuestionDto {
  id: number;
  text: string;
  helpText: string | null;
  multiple: boolean;
  order: number;
  options: QuizOptionDto[];
}

export interface QuizDto {
  version: number;
  questions: QuizQuestionDto[];
}

export interface QuizAnswer {
  questionId: number;
  optionIds: number[];
}

export interface QuizScoreResponse {
  primary: StyleDto;
  secondary: StyleDto;
  scores: Record<string, number>;
}

export interface LessonSummaryDto {
  id: number;
  title: string;
  summary: string;
  order: number;
  freeSample: boolean;
  coverUrl: string | null;
  minutes: number;
}

export interface LessonDetailDto {
  id: number;
  title: string;
  mainIdea: string;
  examples: { imageUrl: string; text: string }[];
  tip: string;
  review: { question: string; options: string[]; correctIndex: number } | null;
  freeSample: boolean;
  usesColorProfile: boolean;
}

export type QuickAction = 'MORE_CASUAL' | 'MORE_FORMAL' | 'ANOTHER';

/** Perfil que se envía al asesor: sin correo ni apellidos. */
export interface AdvisorProfile {
  name?: string;
  ageRange?: string;
  garmentPronoun?: string;
  primaryStyle?: string;
  secondaryStyle?: string;
  skinTone?: string;
  hairColor?: string;
  eyeColor?: string;
  climate?: string;
  budget?: string;
  occasions: string[];
}

export interface WardrobeItem {
  name?: string;
  category: string;
  color: string;
}

export interface AdvisorRequest {
  message: string;
  quickAction?: QuickAction;
  profile: AdvisorProfile;
  wardrobe: WardrobeItem[];
  history: { role: 'USER' | 'ASSISTANT'; text: string }[];
}

export interface OutfitSuggestion {
  title: string;
  items: string[];
  why: string;
}

export interface AdvisorReply {
  reply: string;
  outfits: OutfitSuggestion[];
  followUpQuestion: string | null;
  remainingMessages: number;
}

export type ExploreSection = 'FOR_YOUR_STYLE' | 'TRENDS' | 'ACCESSORIES';

export interface ExploreItemDto {
  id: number;
  name: string;
  imageUrl: string;
  imageCredit: string | null;
  category: string;
  color: string;
  styles: string[];
  whyItFits: string;
  trend: boolean;
  accessory: boolean;
}

export interface ExploreItemDetailDto extends ExploreItemDto {
  combineIdeas: { context: string; items: string[] }[];
}

export interface WardrobeSuggestionDto {
  category: string;
  title: string;
  reason: string;
  items: ExploreItemDto[];
}

export interface AppStoreDto {
  id: number;
  name: string;
  logoUrl: string | null;
  clothingTypes: string[];
  url: string;
  affiliate: boolean;
}
