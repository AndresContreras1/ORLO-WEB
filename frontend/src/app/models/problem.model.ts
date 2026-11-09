/** Error del backend en formato ProblemDetail. */
export interface Problem {
  status: number;
  title: string;
  detail: string;
  code?: string;
  errors?: Record<string, string>;
}
