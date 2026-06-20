export interface RegisterRequest {
  username: string;
  profileName: string;
  email: string;
  password: string;
  passwordConfirm: string;
  dateOfBirth: string;
  termsAccepted: boolean;
}
