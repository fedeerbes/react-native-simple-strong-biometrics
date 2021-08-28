import { NativeModules } from 'react-native';

interface SimpleStrongBiometricsAndroidDialogStrings {
  header?: string;
  description?: string;
  cancel?: string;
}

export interface SimpleStrongBiometricsConfigInterface {
  sharedPreferencesName: string;
}

export interface GetItemConfigInterface
  extends SimpleStrongBiometricsConfigInterface {
  dialogStrings?: SimpleStrongBiometricsAndroidDialogStrings;
}

type SimpleStrongBiometricsType = {
  getItem(key: string, options: GetItemConfigInterface): Promise<string>;
  setItem(
    key: string,
    value: string,
    options: SimpleStrongBiometricsConfigInterface
  ): Promise<null>;
  deleteItem(
    key: string,
    options: SimpleStrongBiometricsConfigInterface
  ): Promise<null>;
  hasStrongBiometricEnabled(): Promise<boolean>;
};

const { SimpleStrongBiometrics } = NativeModules;

export default SimpleStrongBiometrics as SimpleStrongBiometricsType;
