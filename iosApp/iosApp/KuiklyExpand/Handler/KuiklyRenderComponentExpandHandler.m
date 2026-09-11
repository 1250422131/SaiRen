#import "KuiklyRenderComponentExpandHandler.h"
#import <SDWebImage/UIImageView+WebCache.h>
#import <SDWebImage/SDWebImageManager.h>
#import <SVGKit/SVGKImage.h>

@implementation KuiklyRenderComponentExpandHandler

+ (void)load {
    // 注册自定义实现
    [KuiklyRenderBridge registerComponentExpandHandler:[self new]];
}

/*
 * 自定义实现设置图片
 * 使用当前 Kuikly 图片适配回调，确保 SVG 图片也会进入框架的着色和加载完成流程。
 * @return 是否处理该图片设置，返回值为YES，则交给该代理实现，否则sdk内部自己处理
 */
- (BOOL)hr_setImageWithUrl:(NSString *)url
                imageParams:(NSDictionary *)imageParams
                    complete:(ImageCompletionBlock)completeBlock {
    NSURL *imageUrl = [NSURL URLWithString:url];
    if (imageUrl.isFileURL && [[imageUrl.pathExtension lowercaseString] isEqualToString:@"svg"]) {
        SVGKImage *svgImage = [SVGKImage imageWithContentsOfURL:imageUrl];
        UIImage *image = svgImage.UIImage;
        NSError *error = nil;
        if (image == nil) {
            error = [NSError errorWithDomain:@"SaiRen.SVG"
                                         code:1
                                     userInfo:@{NSLocalizedDescriptionKey: @"SVG 图片解析失败"}];
        }
        completeBlock(image, error, imageUrl);
        return YES;
    }

    [[SDWebImageManager sharedManager] loadImageWithURL:imageUrl
                                                 options:0
                                                progress:nil
                                               completed:^(UIImage *image, NSData *data, NSError *error, SDImageCacheType cacheType, BOOL finished, NSURL *imageURL) {
        if (finished) {
            completeBlock(image, error, imageURL);
        }
    }];
    return YES;
}
/*
 * 自定义实现设置颜值
 * @param value 设置的颜色值
 * @return 完成自定义处理的颜色对象
 */
- (UIColor *)hr_colorWithValue:(NSString *)value {
    return nil;
}

@end
