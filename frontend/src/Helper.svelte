<script>
    import tabFocus from "./actions/tabFocus";
    import { clickOutside } from './actions/clickOutside';
    import IconButton from "./IconButton.svelte";
    import { isPortraitOrientation } from "./common/dom";
    import { push } from "./common/arrays";

    export let tooltipIndex = 0;
    export let open = false;

    let tooltips = [];

    $: {
        tooltips = [
            {
                title: "Название",
                text: "Введите свое название в поле над текстом.",
            },
            {
                title: "Новая строка",
                text: "Для перехода на новую строку нажмите Enter или кнопку + в конце текущей строки.",
            },
            {
                title: "Новая строфа",
                text: "Для перехода на новую строфу перейдите на новую строку и нажмите Enter.",
            },
            {
                title: "Удаление",
                text: "Для удаления текста работают клавиши Delete / Backspace. " +
                    "После удаления всего содержимого строки происходит переход на другую строку. " +
                    "При удалении последней строки в строфе происходит переход на другую строфу.",
            },
            {
                title: "Подбор рифмы",
                text: "Нажмите на иконку с лампочкой в конце строки, чтобы начать подбор рифмы. " +
                "Далее необходимо выбрать слово, к которому будет подбираться рифма. " +
                isPortraitOrientation() ?
                    "Это можно сделать, нажав на слово в конце какой-либо строки. " :
                    "Это можно сделать, нажав на слово в конце какой-либо строки или перемещаясь с помощью стрелок вправо-влево, вверх-вниз. " +
                    "Когда выбор сделан, нажмите Enter или кнопку ✓ в окне подсказки. " +
                    "Выйти из режима выбора рифмы можно нажав Esc, кнопку х в окне подсказки, иконку лампочки или просто кликнув в любом месте страницы."
            },
            {
                title: "Выбор варианта рифмы",
                text: "Когда рифма будет подобрана, появится выпадающий список с вариантами выбора. " +
                    "Чтобы сохранить выбор, нажмите Enter или кнопку ✓ в окне подсказки."
            },
            {
                title: "Выбор ударения",
                text: "В некоторых словах могут быть разные варианты ударения. " +
                "В таком нужно будет указать ударение (информация об этом появится в окне подсказки). " +
                isPortraitOrientation() ?
                    "Это можно сделать кликнув на нужную букву. " :
                    "Это можно сделать с помощью стрелок вправо-влево или кликнув на нужную букву. " +
                    "Когда выбор сделан, нажмите Enter или кнопку ✓ в окне подсказки.",
            },
        ];

        if (!isPortraitOrientation()) {
            tooltips = push(tooltips, 3,
                {
                    title: "Навигация стрелками",
                    text: "Для навигации по тексту можно использовать стрелки вправо-влево. " +
                        "Для перехода на нижнюю (верхнюю) строку - клавиши вверх (вниз).",
                    mobile: false
                })
            tooltips.push(
                // TODO: buttons for mobile
                {
                    title: "История",
                    text: "Чтобы отменять действия, используйте комбинацию клавиш Ctrl + Z, для возврата отмененных действий - Ctrl + Shift + Z",
                    mobile: false
                }
            )
        }
        tooltips.push(
            {
                title: "Сохранение",
                text: "Текст и история изменений сохраняются только локально в Вашем браузере. " +
                    "Это в том числе означает, что созданный ранее стих не будет отображаться в анонимной вкладке или другом браузере. " +
                    (navigator.clipboard ?
                        "Для сохранения используйте копирование (кнопка в правом верхнем углу) или экспорт (TODO)"
                        : "Для сохранения используйте экспорт (TODO)")
            }
        )
    }

    const infoColor = "#0336FFCC";
    const backText = "Назад";
    const nextText = "Понятно";
    const nextTitle = "Вперед";

    let index;
    let focused = false;
    let tooltip;

    const onClick = () => {
        open = !open;
        // console.trace()
        if (open && tooltip) {
            tooltip.focus();
        }

        console.log('clck', tooltipIndex, open)
    };

    const onBackClick = () => {
        back();
    };

    const onNextClick = () => {
        next();
    };

    const onClickOutside = () => {
        // console.trace()
        reset();
    };

    const reset = () => {
        open = false;
        // tooltipIndex = 0;
        // console.trace()
        // console.log('reset', tooltipIndex, tooltips.length)
    };

    const next = () => {
        console.log('next', tooltipIndex, tooltips.length)
        if (tooltipIndex === tooltips.length - 1) {
            reset();
        } else {
            tooltipIndex++;
        }
    };

    const back = () => {
        if (tooltipIndex > 0) {
            tooltipIndex--;
        }
        console.log('back', tooltipIndex, tooltips.length)
    };

    $: {
        if (tooltipIndex >= tooltips.length) {
            tooltipIndex = 0;
            open = false;
        } else if (tooltipIndex < 0) {
            tooltipIndex = 0;
        }
        index = tooltipIndex;
    }
</script>

<div
        class="container"
        on:clickOutside={onClickOutside}
        use:clickOutside>
    <IconButton
            icon="help" iconSize="30" padding="4" color={infoColor}
            title={"Подсказка"} opacity={open ? 1 : 0.4}
            on:click={onClick}/>

    {#if open}
        <div class="tooltip" bind:this={tooltip}>
            <div class="tooltip-arrow"></div>

            <div class="tooltip-header">
                <b>{tooltips[tooltipIndex].title}</b>
            </div>
            <p>{tooltips[tooltipIndex].text}</p>

            <div class="tooltip-bar">
                <div class="tooltip-button left">
                    <button
                            on:click|stopPropagation={onBackClick}
                            class:key-focus-visible={focused}
                            class="back"
                            use:tabFocus
                            title={backText}
                            on:tabfocus={() => focused = true}
                            on:blur={() => focused = false}>
                        <span class="text">{backText}</span>
                    </button>
                </div>
                <div class="tooltip-button">
                    <button
                            on:click|stopPropagation={onNextClick}
                            class:key-focus-visible={focused}
                            class="next"
                            use:tabFocus
                            title={nextTitle}
                            on:tabfocus={() => focused = true}
                            on:blur={() => focused = false}>
                        <span class="text">{nextText}</span>
                    </button>
                </div>
            </div>
        </div>
    {/if}
</div>

<style lang="scss">
  .container {
    position: relative;
  }

  .tooltip {
    --color: #ffffff;

    position: absolute;
    top: 2rem;
    left: 2.3rem;
    // TODO: dynamic width
    min-width: 20rem;
    background-color: var(--color);
    padding: 0.75rem 0.25rem 0.25rem 0.75rem;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    text-align: left;
    font-size: .8rem;
    z-index: 2;
    box-sizing: border-box;
    border-radius: 0.25rem;
    transform: translateX(-100%);

    & .tooltip-header {
      display: flex;
      justify-content: center;
    }

    & .tooltip-bar {
      display: flex;
      justify-content: center;
      margin-top: 0.75rem;
      margin-bottom: 0.3rem;
    }

    & .tooltip-button {
      &.left {
        margin-right: 1rem;
      }
    }

    & .tooltip-arrow {
      position: absolute;
      top: calc(0rem - 0.7rem);
      left: calc(100% - 1.95rem);

      &::before {
        border-width: 0 0.2rem 0.7rem 0.9rem;
        border-left-color: transparent;
        border-right-color: transparent;
        content: '';
        display: block;
        width: 0;
        height: 0;
        border-style: solid;
        position: absolute;
        color: var(--color);
      }
    }
  }

  button {
    --color: #0336FFCC;

    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0.5rem;
    height: 2rem;
    border-radius: 3rem;

    &.back {
      border: 0.15rem solid var(--color);
      background: none;
      color: var(--color);
    }

    &.next {
      border: none;
      background: var(--color);
      color: white;
    }

    &:hover {
      &:not(:disabled) {
        opacity: 0.7;
      }
    }

    &.key-focus-visible {
      &:not(:disabled) {
        outline: #888888 0.125rem solid;
        opacity: 1;
      }

      &.borders {
        border-color: transparent;
      }
    }
  }

  .text {
    font-weight: 500;
  }
</style>