import React, { useEffect } from 'react';

import { StyleSheet, View, Text, TextInput, Button } from 'react-native';
import SimpleStrongBiometrics from 'react-native-simple-strong-biometrics';

const SHARED_PREFERENCE_NAME = 'sharedPreferencesName';
const KEY_NAME = 'simpleStrongBiometricsKey';

export default function App() {
  const [string, setString] = React.useState('');
  const [decryptedValue, setDecryptedValue] = React.useState('');

  useEffect(() => {
    SimpleStrongBiometrics.hasStrongBiometricEnabled().then((value) =>
      console.log(value ? 'fingerprint' : 'no fingerprint')
    );
  }, []);

  const encrypt = async () => {
    try {
      await SimpleStrongBiometrics.setItem(KEY_NAME, string, {
        sharedPreferencesName: SHARED_PREFERENCE_NAME,
      });
    } catch (error) {
      console.log(error);
    }
  };

  const decrypt = async () => {
    try {
      const decryptedString = await SimpleStrongBiometrics.getItem(KEY_NAME, {
        sharedPreferencesName: SHARED_PREFERENCE_NAME,
        dialogStrings: {
          header: 'SimpleStrongBiometrics Example',
          description: 'Use your fingerprint to unlock the string',
          cancel: 'Cancel',
        },
      });
      console.log('getItem', decryptedString);
      setDecryptedValue(decryptedString);
    } catch (error) {
      console.log(error);
    }
  };

  const remove = async () => {
    try {
      await SimpleStrongBiometrics.deleteItem(KEY_NAME, {
        sharedPreferencesName: SHARED_PREFERENCE_NAME,
      });
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <View style={styles.container}>
      <TextInput value={string} onChangeText={setString} style={styles.input} />
      <View style={styles.actionsContainer}>
        <Button title="ENCRYPT" onPress={encrypt} />
        <Button title="DECRYPT" onPress={decrypt} />
        <Button title="REMOVE" onPress={remove} />
      </View>
      {!!decryptedValue && (
        <Text style={styles.decryptedValue}>{decryptedValue}</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginTop: 100,
    padding: 20,
  },
  input: {
    borderBottomWidth: 1,
  },
  actionsContainer: {
    marginVertical: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  decryptedValue: {
    color: 'white',
    backgroundColor: 'grey',
    padding: 10,
  },
});
