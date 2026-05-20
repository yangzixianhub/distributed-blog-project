<template>
  <div class="slideshow-shell">
    <a-carousel class="slideshow-content" arrows autoplay>
      <div
          slot="prevArrow"
          slot-scope="props"
          class="custom-slick-arrow"
          style="left: 18px; z-index: 1"
      >
        <a-icon type="left"/>
      </div>
      <div slot="nextArrow" slot-scope="props" class="custom-slick-arrow" style="right: 18px">
        <a-icon type="right"/>
      </div>
      <div class="slide-item" v-for="item of carouselData" :key="item.id">
        <img :src="item.image" :alt="item.title"/>
      </div>
    </a-carousel>
  </div>
</template>

<script>
export default {
  data() {
    return {
      carouselData: [
        {id: 1, title: "banner-1", image: require("@/assets/img/slideshow/banner-1.png")},
        {id: 2, title: "banner-2", image: require("@/assets/img/slideshow/banner-2.png")},
        {id: 3, title: "banner-3", image: require("@/assets/img/slideshow/banner-3.png")},
        {id: 4, title: "banner-4", image: require("@/assets/img/slideshow/banner-4.png")},
        {id: 5, title: "banner-5", image: require("@/assets/img/slideshow/banner-5.png")}
      ]
    };
  }
};
</script>

<style scoped>
.slideshow-shell {
  position: relative;
}

.slide-item {
  height: 320px;
  overflow: hidden;
  border-radius: 28px;
}

.slide-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.02);
}

.ant-carousel >>> .slick-slide {
  border-radius: 28px;
  overflow: hidden;
}

.ant-carousel >>> .custom-slick-arrow {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: 16px;
  color: #fff;
  border-radius: 50%;
  background: rgba(20, 32, 52, 0.36);
  backdrop-filter: blur(8px);
  opacity: 1;
}

.ant-carousel >>> .custom-slick-arrow:before {
  display: none;
}

.ant-carousel >>> .custom-slick-arrow:hover {
  background: rgba(20, 32, 52, 0.6);
}

@media screen and (max-width: 900px) {
  .slide-item {
    height: 220px;
    border-radius: 22px;
  }
}
</style>

