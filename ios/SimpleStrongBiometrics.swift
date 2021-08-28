@objc(SimpleStrongBiometrics)
class SimpleStrongBiometrics: NSObject {

    
    @objc func getItem(_ key: String, options: NSDictionary, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve("No implemented yet")
    }
    
    
    @objc func setItem(_ key: String, value: String, options: NSDictionary, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(nil)
    }
    
    @objc func deleteItem(_ key: String, options: NSDictionary, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(nil)
    }
    
    @objc func hasStrongBiometricEnabled(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(false)
    }
}
