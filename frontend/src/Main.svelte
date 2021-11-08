<script>
    import Poem from "./Poem.svelte";
    import Helper from "./Helper.svelte";
    import * as history from "./data/localhistory";

    const email = "anastasia.rv611@gmail.com";

    const startYear = 2021;
    const date = new Date();
    const year = date.getFullYear();
    let yearRecord;

    export let stanzas = [];
    export let poemTitle = 'Silentium';

    const pageTitle = 'Поэт.Онлайн';

    let revision = 0;
    const storedData = history.getLatest();
    if (storedData) {
        const { data, revision } = history.getLatest();
        stanzas = data && data.stanzas || stanzas;
        poemTitle = data && data.title || poemTitle;
    } else {
        history.save(poemTitle, stanzas);
    }

    let tooltipIndex = 0;
    let isTooltipOpen = false;
    let poemChanged = false;

    window.onkeydown = (e) => {
        if (isTooltipOpen) {
            e.preventDefault();

            if (e.key === 'Escape') {
                isTooltipOpen = false;
            } else if (e.key === 'Enter') {
                tooltipIndex++;
            } else if (e.key === 'ArrowLeft') {
                tooltipIndex--;
            } else if (e.key === 'ArrowRight') {
                tooltipIndex++;
            }
        }

        if (e.ctrlKey || e.metaKey) {
            if (e.key === 'z') {
                e.preventDefault();

                if (e.shiftKey) {
                    console.log('NEXT');
                    history.next();
                } else {
                    console.log('BACK');
                    history.back();
                }

                const storedData = history.getCurrent();
                if (storedData) {
                    console.log('STORED', storedData.data.stanzas)
                    const { data, revision } = storedData;
                    stanzas = data && data.stanzas || stanzas;
                    poemTitle = data && data.title || poemTitle;
                }
            }
        }
    };

    const onPoemChange = ({detail}) => {
        poemChanged = true;
        console.trace()
        console.log('CH')
        stanzas = detail.stanzas;
        poemTitle = detail.title;
        history.save(poemTitle, stanzas, revision);
    };

    $: {
        if (startYear === year) {
            yearRecord = year;
        } else {
            yearRecord = `${startYear} - ${year}`;
        }
    }
</script>

<header>
    <span class="title">
        {pageTitle}
    </span>
    <ul>
        <li></li>
        <li>
            <Helper bind:open={isTooltipOpen} bind:tooltipIndex/>
        </li>
    </ul>
</header>
<main>
    <div class="container">
        <Poem
                bind:stanzas
                bind:title={poemTitle}
                on:changed={onPoemChange}
        />
    </div>
</main>
<footer>
    <p>По вопросам улучшений и предложений:</p>
    <p><a href="mailto:{email}">{email}</a></p>
    <p class="year">©&nbsp;{yearRecord}&nbsp;Поэт.Онлайн</p>
</footer>

<style lang="scss">
  @import './styles/_mixins';

  header {
    position: fixed;
    top: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0;
    padding: 1rem 1rem;
    height: 1.5rem;
    width: 100%;
    font-size: 1.5rem;
    background-color: #F4C8BC;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    z-index: 3;

    & .title {
      font-weight: 500;
      color: #200606aa;
    }

    & ul {
      display: flex;
      list-style: none;
      margin: 0;
      margin-right: 2rem;
    }
  }

  main {
    margin-top: 3rem;
    padding: 0.5rem 0.5rem;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .container {
    box-shadow: 0 6px 8px 0 #b7b1ac;
    min-width: 80%;
    max-width: 100%;
    background-color: white;
  }

  footer {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 1rem;
    font-size: x-small;
    & p {
      margin: 0;
    }
    & .year {
      margin: 1rem 0;
    }
  }

  #overlay {
    position: fixed;
    display: none;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 2;
    cursor: pointer;
  }

  @include portrait {
    header {
      padding: 0.75rem 0.75rem;
      font-size: larger;
      box-shadow: 0 2px 4px 0 #b7b1ac;

      & ul {
        margin-right: 1rem;
      }
    }

    main {
      & .container {
        min-width: 20rem;
      }
    }
  }

  @include lg {
    main {
      margin-top: 4rem;
      padding: 1.5rem 0.5rem 2.5rem 0.5rem;
    }

    footer {
      padding-top: 2rem;
      font-size: small;
    }
  }
</style>
