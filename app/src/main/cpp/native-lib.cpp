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
    cur.convertTo(cur, CV_16S);
    ref.convertTo(ref, CV_16S);
    Mat result(cur.rows, cur.cols, CV_16S);
    cv::absdiff(cur, ref, result);
    std::vector<Mat> channel;
    Mat red_channel = channel.at(2);
    Mat sum_channel = channel.at(2) + channel.at(1) + channel.at(0);
    Mat output = red_channel / sum_channel;
    Scalar output_values = sum(output);
    return output_values[0];
}