#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(SimpleStrongBiometrics, NSObject)

RCT_EXTERN_METHOD(getItem: (NSString *)key options:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setItem: (NSString*)key value:(NSString*)value options:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(deleteItem: (NSString *)key options:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(hasStrongBiometricEnabled: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

@end
