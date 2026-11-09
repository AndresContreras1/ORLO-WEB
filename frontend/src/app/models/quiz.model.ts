/** Fila de la tabla de puntos: id de estilo → puntos (0 a 10). */
export type ScoreRow = Record<number, number>;

export interface QuizOption {
  id: number;
  text: string;
  imageUrl: string | null;
  order: number;
  scores: ScoreRow;
}

export interface QuizQuestion {
  id: number;
  text: string;
  helpText: string | null;
  multiple: boolean;
  order: number;
  active: boolean;
  options: QuizOption[];
}

export interface OptionRequest {
  text: string;
  imageUrl: string | null;
  scores: ScoreRow;
}

export interface QuestionRequest {
  text: string;
  helpText: string | null;
  multiple: boolean;
  order: number;
  active: boolean;
  options: OptionRequest[];
}
