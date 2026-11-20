export type BusinessRuleErrorCode = 'REGLE_METIER_VIOLEE';

export interface BusinessRuleError {
  code: BusinessRuleErrorCode;
  message: string;
}
