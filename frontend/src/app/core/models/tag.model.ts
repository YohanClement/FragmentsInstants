export interface Tag {
  id: number;
  name: string;
  isDefault: boolean;
  isProtected: boolean;
}

export interface TagRequest {
  name: string;
  isDefault?: boolean;
  isProtected?: boolean;
}
