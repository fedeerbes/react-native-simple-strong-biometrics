import { NativeModules } from 'react-native';

type SimpleStrongBiometricsType = {
  multiply(a: number, b: number): Promise<number>;
};

const { SimpleStrongBiometrics } = NativeModules;

export default SimpleStrongBiometrics as SimpleStrongBiometricsType;
