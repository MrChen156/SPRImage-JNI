#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_sprimage2_MainActivity_computeFromJni(
        JNIEnv* env,
        jobject /* this */,
        jlong cur_addr,
        jlong ref_addr) {
    using namespace cv;
    Mat& cur = *(Mat*)cur_addr;
    Mat& ref = *(Mat*)ref_addr;
    // 判断大小
    if (cur.rows != ref.rows || cur.cols != ref.cols || cur.channels() != ref.channels()) return -114.0;
    cur.convertTo(cur, CV_32F);
    ref.convertTo(ref, CV_32F);
    Mat result(cur.rows, cur.cols, CV_32F);
    cv::absdiff(cur, ref, result); //cur - ref;
    std::vector<Mat> channel;
    cv::split(result, channel);
    Mat red_channel = channel.at(2);
    Mat sum_channel = channel.at(2) + channel.at(1) + channel.at(0);
    sum_channel.setTo(1,sum_channel==0.0);
    Mat output = red_channel / sum_channel;
    Scalar output_values = sum(output);
    return output_values[0];
}